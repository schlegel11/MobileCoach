package ch.ethz.mobilecoach.model.persistent;

import java.util.Date;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public class MediaLibraryEntry extends ModelObject {
	
	@Getter
	@NonNull
	private ObjectId participantId;
	
	@Getter
	@NonNull
	private String type;
	
	@Getter
	@NonNull
	private String url;
	
	@Getter
	private String title;
	
	@Getter
	private Date createdAt;
	
	
	public static MediaLibraryEntry create(ObjectId participantId, String type, String url, String title) {
		return new MediaLibraryEntry(participantId, type, url, title, new Date());
	}
	
}
