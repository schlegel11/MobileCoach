package ch.ethz.mc.tools;

/* ##LICENSE## */
import java.io.IOException;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;

/**
 * This specializes {@link FileDownloader} in a way, such that both the file
 * name and content can be determined on-demand, i.e. when the user has clicked
 * the component.
 * 
 * @author unknown
 * @see https
 *      ://vaadin.com/wiki/-/wiki/Main/Letting%20the%20user%20download%20a%20f
 *      ile
 */
public class OnDemandFileDownloader extends FileDownloader {

	/**
	 * Provide both the {@link StreamSource} and the filename in an on-demand
	 * way.
	 */
	public interface OnDemandStreamResource extends StreamSource {
		String getFilename();
	}

	private static final long				serialVersionUID	= 1L;
	private final OnDemandStreamResource	onDemandStreamResource;

	public OnDemandFileDownloader(
			final OnDemandStreamResource onDemandStreamResource) {
		super(new StreamResource(onDemandStreamResource, ""));
		this.onDemandStreamResource = onDemandStreamResource;
	}

	@Override
	public boolean handleConnectorRequest(final VaadinRequest request,
			final VaadinResponse response, final String path)
			throws IOException {
		getResource().setFilename(onDemandStreamResource.getFilename());
		return super.handleConnectorRequest(request, response, path);
	}

	private StreamResource getResource() {
		return (StreamResource) getResource("dl");
	}
}
