/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.knn.index.engine.faiss;

import org.opensearch.knn.common.KNNConstants;
import org.opensearch.knn.index.KNNSettings;
import org.opensearch.knn.index.SpaceType;
import org.opensearch.knn.index.engine.AbstractKNNMethod;
import org.opensearch.knn.index.engine.DefaultHnswContext;
import org.opensearch.knn.index.engine.Encoder;
import org.opensearch.knn.index.engine.MethodComponent;
import org.opensearch.knn.index.engine.MethodComponentContext;
import org.opensearch.knn.index.engine.Parameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.opensearch.knn.common.KNNConstants.FAISS_HNSW_DESCRIPTION;
import static org.opensearch.knn.common.KNNConstants.METHOD_ENCODER_PARAMETER;
import static org.opensearch.knn.common.KNNConstants.METHOD_HNSW;
import static org.opensearch.knn.common.KNNConstants.METHOD_PARAMETER_EF_CONSTRUCTION;
import static org.opensearch.knn.common.KNNConstants.METHOD_PARAMETER_EF_SEARCH;
import static org.opensearch.knn.common.KNNConstants.METHOD_PARAMETER_M;

/**
 * Faiss HNSW method implementation
 */
public class FaissHNSWMethod extends AbstractKNNMethod {
    public final static List<SpaceType> SUPPORTED_SPACES = Arrays.asList(
        SpaceType.UNDEFINED,
        SpaceType.HAMMING,
        SpaceType.L2,
        SpaceType.INNER_PRODUCT
    );

    private final static MethodComponentContext DEFAULT_ENCODER_CONTEXT = new MethodComponentContext(
        KNNConstants.ENCODER_FLAT,
        Collections.emptyMap()
    );
    private final static List<Encoder> SUPPORTED_ENCODERS = List.of(new FaissFlatEncoder(), new FaissSQEncoder(), new FaissHNSWPQEncoder());

    /**
     * Constructor for FaissHNSWMethod
     *
     * @see AbstractKNNMethod
     */
    public FaissHNSWMethod() {
        super(initMethodComponent(), Set.copyOf(SUPPORTED_SPACES), new DefaultHnswContext());
    }

    private static MethodComponent initMethodComponent() {
        return MethodComponent.Builder.builder(METHOD_HNSW)
            .addParameter(
                METHOD_PARAMETER_M,
                new Parameter.IntegerParameter(METHOD_PARAMETER_M, KNNSettings.INDEX_KNN_DEFAULT_ALGO_PARAM_M, v -> v > 0)
            )
            .addParameter(
                METHOD_PARAMETER_EF_CONSTRUCTION,
                new Parameter.IntegerParameter(
                    METHOD_PARAMETER_EF_CONSTRUCTION,
                    KNNSettings.INDEX_KNN_DEFAULT_ALGO_PARAM_EF_CONSTRUCTION,
                    v -> v > 0
                )
            )
            .addParameter(
                METHOD_PARAMETER_EF_SEARCH,
                new Parameter.IntegerParameter(METHOD_PARAMETER_EF_SEARCH, KNNSettings.INDEX_KNN_DEFAULT_ALGO_PARAM_EF_SEARCH, v -> v > 0)
            )
            .addParameter(METHOD_ENCODER_PARAMETER, initEncoderParameter())
            .setMapGenerator(
                ((methodComponent, methodComponentContext) -> MethodAsMapBuilder.builder(
                    FAISS_HNSW_DESCRIPTION,
                    methodComponent,
                    methodComponentContext
                ).addParameter(METHOD_PARAMETER_M, "", "").addParameter(METHOD_ENCODER_PARAMETER, ",", "").build())
            )
            .build();
    }

    private static Parameter.MethodComponentContextParameter initEncoderParameter() {
        return new Parameter.MethodComponentContextParameter(
            METHOD_ENCODER_PARAMETER,
            DEFAULT_ENCODER_CONTEXT,
            SUPPORTED_ENCODERS.stream().collect(Collectors.toMap(Encoder::getName, Encoder::getMethodComponent))
        );
    }
}
