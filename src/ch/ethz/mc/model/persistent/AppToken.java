package ch.ethz.mc.model.persistent;

import java.util.Date;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.tools.StringHelpers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public class AppToken extends ModelObject {
	private static final String APPTOKEN_PATTERN = "app:.{50}";
	@Getter
	@NonNull
	private ObjectId participantId;
	
	@Getter
	private String token;
	
	@Getter
	private Date createdAt;

	public static boolean isAppToken(String token) {
		return Pattern.matches(APPTOKEN_PATTERN, token);
	}
	
	public static String createToken() {
		return "app:"+StringHelpers.createRandomString(50);
	}
	
	public static AppToken create(ObjectId participantId) {
		return new AppToken(participantId, createToken(), new Date());
	}
	
}
