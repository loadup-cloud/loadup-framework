package io.github.loadup.components.pipeline.engine;

import io.github.loadup.components.pipeline.api.IBizProcessStage;
import io.github.loadup.components.pipeline.api.IControlStage;
import io.github.loadup.components.pipeline.api.IDataPrepareStage;
import io.github.loadup.components.pipeline.api.IDataProcessStage;
import io.github.loadup.components.pipeline.api.IFinallyStage;
import io.github.loadup.components.pipeline.api.IParamVerifyStage;
import io.github.loadup.components.pipeline.api.IPipelineDefinition;
import io.github.loadup.components.pipeline.api.IResultAssembleStage;
import io.github.loadup.components.pipeline.api.IStage;
import io.github.loadup.components.pipeline.context.PipelineContext;
import io.github.loadup.components.pipeline.exception.ExceptionHandlerBus;
import io.github.loadup.components.pipeline.exception.IExceptionClassHandler;
import io.github.loadup.components.pipeline.spec.PipelineSpec;
import io.github.loadup.components.pipeline.stage.function.IFunctionStage;
import io.github.loadup.components.pipeline.stage.function.InnerBizProcessConsumerStage;
import io.github.loadup.components.pipeline.stage.function.InnerDataPrepareFunctionStage;
import io.github.loadup.components.pipeline.stage.function.InnerDataProcessBiConsumerStage;
import io.github.loadup.components.pipeline.stage.function.InnerParamVerifyConsumerStage;
import io.github.loadup.components.pipeline.stage.function.InnerResultAssembleFunctionStage;
import io.github.loadup.components.pipeline.tx.EndTxMarker;
import io.github.loadup.components.pipeline.tx.ITxInitializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Core pipeline execution engine.
 *
 * <p>The executor iterates the ordered stage list from {@link PipelineSpec}, resolves each
 * stage instance (either a Spring bean or an inline lambda wrapper), and dispatches it by
 * type:
 * <ol>
 *   <li>{@link IParamVerifyStage}  → {@code verify(request)}</li>
 *   <li>{@link IDataPrepareStage}  → {@code prepare(ctx)} — result stored as model</li>
 *   <li>{@link IDataProcessStage} → {@code process(model, ctx)}</li>
 *   <li>{@link IBizProcessStage}  → {@code process(ctx)}</li>
 *   <li>{@link IControlStage}     → if {@code shouldStop(ctx)} returns {@code true}, abort</li>
 *   <li>{@link IResultAssembleStage} → {@code assemble(ctx)} — result stored as response</li>
 *   <li>{@link ITxInitializer}    → begin a Spring transaction wrapping subsequent stages</li>
 *   <li>{@link EndTxMarker}       → end the open transaction</li>
 *   <li>{@link IFinallyStage}     → deferred to the {@code finally} block</li>
 * </ol>
 *
 * <p>Exception handling is delegated to the {@link ExceptionHandlerBus} returned by
 * {@link IPipelineDefinition#exceptions()}.
 */
@Slf4j
@RequiredArgsConstructor
public class PipelineExecutor {

    private final ApplicationContext applicationContext;

    /**
     * Execute a pipeline defined by the given {@link IPipelineDefinition}.
     *
     * @param definition the pipeline definition
     * @param request    the incoming request object
     * @param <RES>      the expected response type
     * @return the assembled response, or the error response if an exception handler ran
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <RES> RES execute(IPipelineDefinition definition, Object request) {
        PipelineContext ctx = new PipelineContext();
        ctx.setRequest(request);

        PipelineSpec spec = definition.definePipeline();
        ExceptionHandlerBus bus = definition.exceptions();

        List<Class<? extends IStage>> stages = spec.getStageList();
        List<IFinallyStage> finallyStages = new ArrayList<>();

        try {
            int i = 0;
            while (i < stages.size()) {
                Class<? extends IStage> stageClass = stages.get(i);

                // ── Collect finally stages — execute them in the finally block ──
                if (IFinallyStage.class.isAssignableFrom(stageClass)
                        && !IFunctionStage.class.isAssignableFrom(stageClass)) {
                    finallyStages.add((IFinallyStage) applicationContext.getBean(stageClass));
                    i++;
                    continue;
                }

                // ── Transaction block: startTx ... endTx ──────────────────────
                if (ITxInitializer.class.isAssignableFrom(stageClass)) {
                    ITxInitializer initializer = (ITxInitializer) applicationContext.getBean(stageClass);
                    org.springframework.transaction.TransactionDefinition txDef = initializer.init(ctx);
                    PlatformTransactionManager txManager = applicationContext.getBean(PlatformTransactionManager.class);
                    TransactionTemplate txTemplate = new TransactionTemplate(txManager, txDef);

                    // Collect the stages inside the tx block
                    int txStart = i + 1;
                    int txEnd = findEndTxIndex(stages, txStart);
                    List<Class<? extends IStage>> txStages = new ArrayList<>(stages.subList(txStart, txEnd));
                    int afterTx = txEnd + 1;

                    PipelineContext ctxRef = ctx;
                    txTemplate.execute(status -> {
                        try {
                            executeStageList(txStages, spec, txStart, ctxRef, finallyStages);
                        } catch (RuntimeException re) {
                            status.setRollbackOnly();
                            throw re;
                        }
                        return null;
                    });

                    i = afterTx;
                    continue;
                }

                // ── Normal stage ──────────────────────────────────────────────
                IStage stage = resolveStage(stageClass, spec, i);
                boolean shouldStop = dispatchStage(stage, ctx);
                if (shouldStop) {
                    break;
                }
                i++;
            }

            return (RES) ctx.getResponse();

        } catch (Throwable t) {
            return (RES) handleException(t, ctx, bus);
        } finally {
            for (IFinallyStage fs : finallyStages) {
                try {
                    fs.doFinally(ctx);
                } catch (Exception e) {
                    log.warn(
                            "[Pipeline] FinallyStage {} threw exception (swallowed)",
                            fs.getClass().getSimpleName(),
                            e);
                }
            }
        }
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void executeStageList(
            List<Class<? extends IStage>> stages,
            PipelineSpec spec,
            int baseOffset,
            PipelineContext ctx,
            List<IFinallyStage> finallyStages) {
        for (int j = 0; j < stages.size(); j++) {
            Class<? extends IStage> sc = stages.get(j);
            if (IFinallyStage.class.isAssignableFrom(sc) && !IFunctionStage.class.isAssignableFrom(sc)) {
                finallyStages.add((IFinallyStage) applicationContext.getBean(sc));
                continue;
            }
            IStage stage = resolveStage(sc, spec, baseOffset + j);
            boolean stop = dispatchStage(stage, ctx);
            if (stop) {
                break;
            }
        }
    }

    private int findEndTxIndex(List<Class<? extends IStage>> stages, int fromIndex) {
        for (int i = fromIndex; i < stages.size(); i++) {
            if (EndTxMarker.class.equals(stages.get(i))) {
                return i;
            }
        }
        // No explicit EndTx — treat end of stage list as implicit end
        return stages.size();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private IStage resolveStage(Class<? extends IStage> stageClass, PipelineSpec spec, int index) {
        // Lambda/inner stages — construct from captured functional interfaces
        if (stageClass == InnerParamVerifyConsumerStage.class) {
            return new InnerParamVerifyConsumerStage(spec.getConsumerIndexMap().get(index));
        }
        if (stageClass == InnerDataPrepareFunctionStage.class) {
            return new InnerDataPrepareFunctionStage(spec.getFunctionIndexMap().get(index));
        }
        if (stageClass == InnerBizProcessConsumerStage.class) {
            return new InnerBizProcessConsumerStage(spec.getConsumerIndexMap().get(index));
        }
        if (stageClass == InnerDataProcessBiConsumerStage.class) {
            return new InnerDataProcessBiConsumerStage(
                    spec.getBiConsumerIndexMap().get(index));
        }
        if (stageClass == InnerResultAssembleFunctionStage.class) {
            return new InnerResultAssembleFunctionStage(
                    spec.getFunctionIndexMap().get(index));
        }
        // Spring-managed bean
        return applicationContext.getBean(stageClass);
    }

    /**
     * Dispatch a resolved stage to the correct interface method.
     *
     * @return {@code true} if the pipeline should stop (IControlStage said so)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean dispatchStage(IStage stage, PipelineContext ctx) {
        if (stage instanceof IParamVerifyStage vs) {
            vs.verify(ctx.getRequest());

        } else if (stage instanceof IDataPrepareStage ds) {
            Object model = ds.prepare(ctx);
            if (model != null) {
                ctx.setModel(model);
            }

        } else if (stage instanceof IDataProcessStage dps) {
            dps.process(ctx.getModel(), ctx);

        } else if (stage instanceof IBizProcessStage bps) {
            bps.process(ctx);

        } else if (stage instanceof IControlStage cs) {
            return cs.shouldStop(ctx);

        } else if (stage instanceof IResultAssembleStage ras) {
            Object response = ras.assemble(ctx);
            ctx.setResponse(response);

        } else if (stage instanceof IFinallyStage) {
            // Should not reach here — finally stages are collected before dispatch
            log.warn(
                    "[Pipeline] IFinallyStage {} reached dispatchStage — possible misconfiguration",
                    stage.getClass().getSimpleName());
        }
        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object handleException(Throwable t, PipelineContext ctx, ExceptionHandlerBus bus) {
        // 1. Check overflow — rethrow as-is
        for (Class<? extends Throwable> overflowClass : bus.getOverflowExceptions()) {
            if (overflowClass.isInstance(t)) {
                log.debug(
                        "[Pipeline] Overflow exception {}, rethrowing",
                        t.getClass().getSimpleName());
                sneakyThrow(t);
            }
        }

        // 2. Find the most specific registered handler (exact match first, then walk hierarchy)
        Class<? extends IExceptionClassHandler<?>> handlerClass = null;
        Map<Class<? extends Throwable>, Class<? extends IExceptionClassHandler<?>>> handlerMap =
                bus.getExceptionHandlerMap();

        for (Map.Entry<Class<? extends Throwable>, Class<? extends IExceptionClassHandler<?>>> entry :
                handlerMap.entrySet()) {
            if (entry.getKey().equals(t.getClass())) {
                handlerClass = entry.getValue();
                break;
            }
        }
        if (handlerClass == null) {
            for (Map.Entry<Class<? extends Throwable>, Class<? extends IExceptionClassHandler<?>>> entry :
                    handlerMap.entrySet()) {
                if (entry.getKey().isInstance(t)) {
                    handlerClass = entry.getValue();
                    break;
                }
            }
        }

        // 3. Fallback to bottom handler
        if (handlerClass == null) {
            handlerClass = bus.getBottomHandler();
        }

        if (handlerClass == null) {
            log.error("[Pipeline] No exception handler configured and no bottom handler set", t);
            sneakyThrow(t);
        }

        IExceptionClassHandler handler = applicationContext.getBean(handlerClass);
        handler.handleException(t, ctx);
        return handler.assembleResultOnException(t, ctx);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}
