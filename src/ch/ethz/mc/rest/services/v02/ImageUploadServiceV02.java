package ch.ethz.mc.rest.services.v02;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.ImplementationConstants.ACCEPTED_MEDIA_UPLOAD_TYPES;
import ch.ethz.mc.model.rest.UploadOK;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService.FILE_STORES;
import ch.ethz.mc.services.internal.VariablesManagerService.ExternallyWriteProtectedVariableException;
import ch.ethz.mc.tools.StringValidator;

/**
 * Service to allow upload of images using REST
 *
 * @author Andreas Filler
 */
@Path("/v02/image")
@Log4j2
public class ImageUploadServiceV02 extends AbstractFileUploadServiceV02 {
	RESTManagerService restManagerService;

	public ImageUploadServiceV02(final RESTManagerService restManagerService) {
		super(restManagerService);
		this.restManagerService = restManagerService;
	}

	@POST
	@Path("/upload/{variable}")
	@Consumes("multipart/form-data")
	@Produces("application/json")
	public Response imageUpload(@HeaderParam("user") final String user,
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request,
			final MultipartFormDataInput input) {
		log.debug("Token {}: Upload image to variable {}", token, variable);
		ObjectId participantId;
		try {
			participantId = checkExternalParticipantAccessAndReturnParticipantId(
					user, token);
		} catch (final Exception e) {
			throw e;
		}

		log.debug("Size of image upload: {}", request.getContentLength());
		if (request
				.getContentLength() > ImplementationConstants.MAX_UPLOAD_SIZE_IN_BYTE) {
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST)
							.entity("Could not upload image: The image file is too big")
							.build());
		}

		if (!StringValidator.isValidVariableName(
				ImplementationConstants.VARIABLE_PREFIX + variable.trim())) {
			throw new WebApplicationException(Response.serverError()
					.entity("Could not upload image: The variable name is not valid")
					.build());
		}

		val uploadToVariableAllowed = restManagerService
				.checkVariableForServiceWritingRights(participantId, variable);
		if (uploadToVariableAllowed == false) {
			throw new WebApplicationException(
					Response.serverError()
							.entity("The variable " + variable
									+ " cannot be written by the participant")
							.build());
		}

		// Do upload to temporary file
		val mediaType = ACCEPTED_MEDIA_UPLOAD_TYPES.IMAGE;
		File temporaryFile = null;
		try {
			temporaryFile = handleUpload(input, mediaType);
		} catch (final Exception e) {
			log.warn("Error at handling {} file upload: {}", mediaType,
					e.getMessage());

			if (temporaryFile != null && temporaryFile.exists()) {
				try {
					temporaryFile.delete();
				} catch (final Exception f) {
					// Nothing to do
				}
			}

			throw new WebApplicationException(Response.serverError()
					.entity("Could not upload image: " + e.getMessage())
					.build());
		}

		// Store temporary file properly
		final String fileReference = handleStoring(temporaryFile);

		if (temporaryFile != null && temporaryFile.exists()) {
			try {
				temporaryFile.delete();
			} catch (final Exception f) {
				// Nothing to do
			}
		}

		if (fileReference == null) {
			throw new WebApplicationException(Response.serverError()
					.entity("Image could not be moved to repository").build());
		}

		// Store reference to variable
		try {
			restManagerService.writeVariable(participantId, variable,
					fileReference.replace("/", "-"), true, true);
		} catch (final ExternallyWriteProtectedVariableException e) {
			try {
				restManagerService.getFileStorageManagerService()
						.deleteFile(fileReference, FILE_STORES.MEDIA_UPLOAD);
			} catch (final Exception f) {
				// Nothing to do
			}

			throw new WebApplicationException(
					Response.serverError()
							.entity("The variable " + variable
									+ " cannot be written by the participant")
							.build());
		}

		return Response.ok(new UploadOK(fileReference.replace("/", "-")))
				.build();
	}
}
