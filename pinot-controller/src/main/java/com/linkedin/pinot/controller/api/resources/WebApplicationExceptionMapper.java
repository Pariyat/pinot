/**
 * Copyright (C) 2014-2016 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.pinot.controller.api.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.linkedin.pinot.common.config.TableNameBuilder;
import com.linkedin.pinot.common.restlet.resources.SegmentSizeInfo;
import com.linkedin.pinot.common.utils.CommonConstants;
import com.linkedin.pinot.controller.helix.core.PinotHelixResourceManager;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<Throwable> {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);
  private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

  @Override
  public Response toResponse(Throwable t) {
    int status = 500;
    if (! (t instanceof WebApplicationException)) {
      LOGGER.error("Server error: ", t);
    } else {
      status = ((WebApplicationException)t ).getResponse().getStatus();
    }

    ErrorInfo einfo = new ErrorInfo(status, t.getMessage());
    try {
      return Response.status(status).entity(JSON_MAPPER.writeValueAsString(einfo))
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (JsonProcessingException e) {
      String err = String.format("{\"status\":%d, \"error\":%s}", einfo.code, einfo.error);
      return Response.status(status).entity(err)
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
  }

  public static class ErrorInfo {
    @JsonCreator
    public ErrorInfo(@JsonProperty("code") int code, @JsonProperty("error") String message) {
      this.code = code;
      this.error = message;
    }
    public int code;
    public String error;
  }
}
