package io.github.loadup.components.pipeline.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Shared mutable state bag passed across all pipeline stages.
 *
 * <p><strong>Access contract:</strong>
 * <ul>
 *   <li>Stages may call {@link #getRequest()}, {@link #getModel(Class)}, and the property
 *       accessors freely.</li>
 *   <li>Only {@link io.github.loadup.components.pipeline.engine.PipelineExecutor} should call
 *       the {@code set*} mutators — consider them internal API.</li>
 * </ul>
 */
public class PipelineContext {

    /** The original request object passed to the pipeline executor. */
    private Object request;

    /** The domain model set by {@link io.github.loadup.components.pipeline.api.IDataPrepareStage}. */
    private Object model;

    /** Runtime class of {@link #model}, used for safe casting. */
    private Class<?> modelClass;

    /** The response object set by {@link io.github.loadup.components.pipeline.api.IResultAssembleStage}. */
    private Object response;

    /**
     * Arbitrary key-value bag for cross-stage communication.
     * Prefer typed getters/setters over raw map access when possible.
     */
    private final Map<String, Object> properties = new HashMap<>();

    // ── Public read API ─────────────────────────────────────────────────────

    /**
     * Returns the raw request object.
     *
     * @param <REQ> expected type
     * @param type  the expected request class
     * @return the request cast to {@code type}
     */
    @SuppressWarnings("unchecked")
    public <REQ> REQ getRequest(Class<REQ> type) {
        return type.cast(request);
    }

    /** Returns the raw request without casting — useful in generic stages. */
    public Object getRequest() {
        return request;
    }

    /**
     * Returns the domain model prepared by the data-prepare stage.
     *
     * @param <DATA> expected model type
     * @param type   the expected model class
     * @return the model cast to {@code type}; may be {@code null} if no prepare stage ran
     */
    @SuppressWarnings("unchecked")
    public <DATA> DATA getModel(Class<DATA> type) {
        if (Objects.isNull(model)) {
            return null;
        }
        return type.cast(model);
    }

    /** Returns the raw model without casting. */
    public Object getModel() {
        return model;
    }

    /**
     * Returns the final response object.
     *
     * @param <RES> expected response type
     * @param type  the expected response class
     */
    @SuppressWarnings("unchecked")
    public <RES> RES getResponse(Class<RES> type) {
        return type.cast(response);
    }

    /** Returns the raw response without casting. */
    public Object getResponse() {
        return response;
    }

    /**
     * Returns a property stored in this context.
     *
     * @param key the property key
     * @param <T> expected value type
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    /**
     * Stores a property in this context for downstream stages to read.
     *
     * @param key   the property key
     * @param value the value; {@code null} removes the entry
     */
    public void putProperty(String key, Object value) {
        if (value == null) {
            properties.remove(key);
        } else {
            properties.put(key, value);
        }
    }

    // ── Internal mutators (used by PipelineExecutor) ─────────────────────────

    /** @internal used by {@link io.github.loadup.components.pipeline.engine.PipelineExecutor} */
    public void setRequest(Object request) {
        this.request = request;
    }

    /** @internal used by {@link io.github.loadup.components.pipeline.engine.PipelineExecutor} */
    public void setModel(Object model) {
        this.model = model;
        this.modelClass = model.getClass();
    }

    /** @internal used by {@link io.github.loadup.components.pipeline.engine.PipelineExecutor} */
    public void setResponse(Object response) {
        this.response = response;
    }

    /** Returns the runtime class of the model (null if no prepare stage ran). */
    public Class<?> getModelClass() {
        return modelClass;
    }
}
