package ch.ethz.mc.model;

import org.jongo.MongoCollection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IndexSpecTest {

	@Mock
	private MongoCollection collection;

	@Test
	public void testIndexOn_no_options_OneArgMethodCalled() {
		String indexSpecStr = "{'field':1}";
		new IndexSpec(indexSpecStr).ensureOn(collection);
		verify(collection).ensureIndex(indexSpecStr);
	}

	@Test
	public void testIndexOn_with_options_TwoArgMethodCalled() {
		String indexSpecStr = "{'field':1}";
		String indexSpecOptions = "{unique: true}";
		new IndexSpec(indexSpecStr, indexSpecOptions).ensureOn(collection);
		verify(collection).ensureIndex(indexSpecStr, indexSpecOptions);
	}
	
	@Test
	public void testCreateOneElement() {
		IndexSpec.create(new String[]{"1"}).ensureOn(collection);
		verify(collection).ensureIndex("1");
	}
	
	@Test
	public void testCreateTwoElementsSecondNull() {
		IndexSpec.create(new String[]{"1", null}).ensureOn(collection);
		verify(collection).ensureIndex("1");
	}
	
	@Test
	public void testCreateTwoElementsSecondEmpty() {
		IndexSpec.create(new String[]{"1", "    "}).ensureOn(collection);
		verify(collection).ensureIndex("1");
	}
	
	@Test
	public void testCreateTwoElementsSecondNonEmpty() {
		IndexSpec.create(new String[]{"1", "2"}).ensureOn(collection);
		verify(collection).ensureIndex("1", "2");
	}

}
