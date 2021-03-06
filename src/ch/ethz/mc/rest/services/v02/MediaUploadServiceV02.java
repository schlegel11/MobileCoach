package ch.ethz.mc.rest.services.v02;

/* ##LICENSE## */
import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.types.ObjectId;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.ImplementationConstants.ACCEPTED_MEDIA_UPLOAD_TYPES;
import ch.ethz.mc.model.persistent.BackendUserInterventionAccess;
import ch.ethz.mc.model.rest.CollectionOfVariablesWithTimestamp;
import ch.ethz.mc.model.rest.UploadOK;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService.FILE_STORES;
import ch.ethz.mc.services.internal.VariablesManagerService.ExternallyWriteProtectedVariableException;
import ch.ethz.mc.tools.StringValidator;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Service to allow upload of media content using REST
 *
 * @author Andreas Filler
 */
@Path("/v02/media")
@Log4j2
public class MediaUploadServiceV02 extends AbstractFileUploadServiceV02 {
	RESTManagerService restManagerService;

	public MediaUploadServiceV02(final RESTManagerService restManagerService) {
		super(restManagerService);
		this.restManagerService = restManagerService;
	}

	@POST
	@Path("/upload/{mediaType}/{variable}")
	@Consumes("multipart/form-data")
	@Produces("application/json")
	public Response mediaUpload(@HeaderParam("user") final String user,
			@HeaderParam("token") final String token,
			@PathParam("mediaType") final ACCEPTED_MEDIA_UPLOAD_TYPES mediaType,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request,
			final MultipartFormDataInput input) {

		log.debug("Token {}: Upload {} to variable {}", token, mediaType,
				variable);
		ObjectId participantId;
		try {
			participantId = checkExternalParticipantAccessAndReturnParticipantId(
					user, token);
		} catch (final Exception e) {
			throw e;
		}

		log.debug("Size of media upload: {}", request.getContentLength());

		int maxUploadSize = 0;
		switch (mediaType) {
			case IMAGE:
				maxUploadSize = ImplementationConstants.MAX_IMAGE_UPLOAD_SIZE_IN_BYTE;
				break;
			case VIDEO:
				maxUploadSize = ImplementationConstants.MAX_VIDEO_UPLOAD_SIZE_IN_BYTE;
				break;
			case AUDIO:
				maxUploadSize = ImplementationConstants.MAX_AUDIO_UPLOAD_SIZE_IN_BYTE;
				break;
		}

		if (request.getContentLength() > maxUploadSize) {
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST)
							.entity("Could not upload media file: The file is too big")
							.build());
		}

		if (!StringValidator.isValidVariableName(
				ImplementationConstants.VARIABLE_PREFIX + variable.trim())) {
			throw new WebApplicationException(Response.serverError()
					.entity("Could not media file image: The variable name is not valid")
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
					.entity("Could not upload media file: " + e.getMessage())
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
					.entity("Media file could not be moved to repository")
					.build());
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

	@GET
	@Path("/externallyReadUploadsOfParticipant/{group}/{participant}")
	@Produces("application/json")
	public CollectionOfVariablesWithTimestamp externallyReadUploadsOfParticipant(
			@HeaderParam("user") final String user,
			@HeaderParam("password") final String password,
			@HeaderParam("interventionPattern") final String interventionPattern,
			@PathParam("group") final String group,
			@PathParam("participant") final String participant) {
		log.debug("Externally read uploads of participant {} from group {}",
				participant, group);
		BackendUserInterventionAccess backendUserInterventionAccess;
		try {
			backendUserInterventionAccess = checkExternalBackendUserInterventionAccess(
					user, password, group, interventionPattern);
		} catch (final Exception e) {
			throw e;
		}

		try {
			if (!ObjectId.isValid(participant.trim())) {
				throw new Exception("The participant id is not valid");
			}

			val collectionOfVariablesWithTimestamp = restManagerService
					.readUploadsOfParticipantIfPartOfInterventionAndGroup(
							backendUserInterventionAccess.getIntervention(),
							group, new ObjectId(participant.trim()));

			return collectionOfVariablesWithTimestamp;
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve uploads of participant: "
							+ e.getMessage())
					.build());
		}
	}
}
