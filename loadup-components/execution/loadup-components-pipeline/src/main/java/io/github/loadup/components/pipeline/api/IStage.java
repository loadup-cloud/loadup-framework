package io.github.loadup.components.pipeline.api;

/**
 * Marker interface for all pipeline stages.
 *
 * <p>Stage hierarchy:
 * <pre>
 * IStage
 * ├── IParamVerifyStage   — parameter validation
 * ├── IDataPrepareStage   — data / domain-model loading
 * ├── IProcessStage
 * │   ├── IBizProcessStage   — pure business logic
 * │   └── IDataProcessStage  — data transformation with model
 * ├── IResultAssembleStage — DO → DTO mapping
 * ├── IControlStage        — conditional flow control
 * └── IFinallyStage        — always executed (cleanup)
 * </pre>
 */
public interface IStage {}
