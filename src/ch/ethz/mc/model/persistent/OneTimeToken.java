package ch.ethz.mc.model.persistent;

import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public class OneTimeToken extends ModelObject {
	
	private static final String ONETIMETOKEN_PATTERN = "[0-9A-Z]{6}";
	
	@Getter
	@NonNull
	private ObjectId participantId;
	
	@Getter
	private String token;
	
	@Getter
	private Date createdAt;
	
	

	public static boolean isOneTimeToken(String token) {
		return Pattern.matches(ONETIMETOKEN_PATTERN, token);
	}
	
	public static String createToken() {
		StringBuilder sb = new StringBuilder();
		Random r = new Random();
		for(int i=0; i<6; i++) {
			sb.append(Integer.toString(r.nextInt(36), Character.MAX_RADIX).toUpperCase());
		}
		return sb.toString();
	}
	
	public static OneTimeToken create(ObjectId participantId) {
		return new OneTimeToken(participantId, createToken(), new Date());
	}
	
}
