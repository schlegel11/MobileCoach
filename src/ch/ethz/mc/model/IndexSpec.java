package ch.ethz.mc.model;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.jongo.MongoCollection;

import com.google.common.base.Optional;

public class IndexSpec {
	
	private final String indexSpecStr;
	private final Optional<String> indexSpecOptions;

	public IndexSpec(String indexSpecStr) {
		this(indexSpecStr, null);
	}

	public IndexSpec(String indexSpecStr, String indexSpecOptions) {
		this.indexSpecStr = indexSpecStr;
		this.indexSpecOptions = Optional.fromNullable(indexSpecOptions);
	}
	
	public static IndexSpec create(String[] specWithOpions) {
		if (specWithOpions.length == 1) {
			return new IndexSpec(specWithOpions[0]);
		}
		if (specWithOpions.length == 2) {
			if (StringUtils.isBlank(specWithOpions[1])) {
				return new IndexSpec(specWithOpions[0]);
			}
			return new IndexSpec(specWithOpions[0], specWithOpions[1]);
		}
		throw new IllegalArgumentException("Length of array passed should be 1 or 2. But it contained "+Arrays.toString(specWithOpions));
	}

	public void ensureOn(MongoCollection collection) {
		if (indexSpecOptions.isPresent()) {
			collection.ensureIndex(indexSpecStr, indexSpecOptions.get());
		} else {
			collection.ensureIndex(indexSpecStr);
		}
	}

}
