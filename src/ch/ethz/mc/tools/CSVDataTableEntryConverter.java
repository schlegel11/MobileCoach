package ch.ethz.mc.tools;

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
import ch.ethz.mc.model.memory.DataTable;
import ch.ethz.mc.model.memory.DataTable.DataEntry;
import ch.ethz.mc.model.ui.results.UIVariableWithParticipantForResults;

import com.googlecode.jcsv.writer.CSVEntryConverter;

/**
 * Converter to convert {@link UIVariableWithParticipantForResults} to CSV
 * 
 * @author Andreas Filler
 */
public class CSVDataTableEntryConverter implements CSVEntryConverter<DataEntry> {

	@Override
	public String[] convertEntry(final DataEntry dataEntry) {
		return dataEntry.toStringArray();
	}

	public static DataEntry getHeaders(final DataTable dataTable) {
		return dataTable.getHeaders();
	}

}