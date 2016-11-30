package ch.ethz.mobilecoach.services.test;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;

import ch.ethz.mobilecoach.app.Post;


public class SerializationTest {

    @Test
    public void testPostSerialization() throws Exception {
    	
    	Post post = new Post();
    	
    	String rt = "request type 1";
    	String pt = "post type 1";
    	post.setRequestType(rt);    	
    	post.setPostType(pt); 	  
    	
    	JSONObject jo = new JSONObject(post);
    	
    	String s = jo.toString();
    	
    	assertTrue(s.length() > 0);
    	assertEquals(pt, jo.getString("post_type"));
    	assertEquals(rt, jo.getString("request_type"));
    }
    
   
		

}