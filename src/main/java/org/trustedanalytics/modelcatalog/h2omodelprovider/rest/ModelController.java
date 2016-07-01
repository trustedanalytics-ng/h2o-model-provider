/**
 * Copyright (c) 2016 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.modelcatalog.h2omodelprovider.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.trustedanalytics.modelcatalog.h2omodelprovider.service.ModelService;
import org.trustedanalytics.modelcatalog.rest.api.ModelMetadata;
import org.trustedanalytics.modelcatalog.rest.api.ModelProviderApi;
import org.trustedanalytics.modelcatalog.rest.api.ModelProviderPaths;

import java.util.Collection;
import java.util.UUID;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class ModelController implements ModelProviderApi {

  ModelService modelService;

  @Autowired
  public ModelController(ModelService modelService) {
    this.modelService = modelService;
  }

  @ApiOperation(
          value = "Get all H2O models in given organization.",
          notes = "Privilege level: Consumer of this endpoint must be a member of specified organization and provide" +
                  " valid token"
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "OK"),
          @ApiResponse(code = 500, message = "Internal server error, e.g. error getting model metadata")
  })
  @RequestMapping(
          value = ModelProviderPaths.GET_ALL_MODELS_PATH,
          method = RequestMethod.GET,
          produces = "application/json; charset=UTF-8")
  @Override
  public Collection<ModelMetadata> listModels(
          @ApiParam(value = "Organization id", required = true) @RequestParam UUID orgId) {
    return modelService.fetchModels(orgId);
  }

  @ApiOperation(
          value = "Get detailed model metadata.",
          notes = "Privilege level: Consumer of this endpoint must be a member of specified organization and provide" +
                  " valid token"
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "OK"),
          @ApiResponse(code = 500, message = "Internal server error, e.g. error getting model metadata")
  })
  @RequestMapping(
          value = ModelProviderPaths.GET_MODEL_METADATA_PATH,
          method = RequestMethod.GET,
          produces = "application/json; charset=UTF-8")
  @Override
  public ModelMetadata fetchModelMetadata(
          @ApiParam(value = "Unique model id", required = true) @PathVariable String modelId) {
    throw new MethodNotImplementedException();
  }

  @ApiOperation(
          value = "Get model binary artifact(s).",
          notes = "Privilege level: Consumer of this endpoint must be a member of specified organization and provide" +
                  " valid token"
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "OK"),
          @ApiResponse(code = 500, message = "Internal server error, e.g. error getting model metadata")
  })
  @RequestMapping(
          value = ModelProviderPaths.GET_MODEL_PATH,
          method = RequestMethod.GET,
          produces = "application/zip")
  @Override
  public FileSystemResource downloadModel(
          @ApiParam(value = "Unique model id", required = true) @PathVariable String modelId) {
    throw new MethodNotImplementedException();
  }

  @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
  private class MethodNotImplementedException extends RuntimeException {
  }
}
