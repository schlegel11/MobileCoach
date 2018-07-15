package ch.ethz.mc.model.persistent;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.concepts.AbstractRule;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.model.ui.UIMicroDialogMessageRule;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.tools.StringHelpers;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * {@link ModelObject} to represent an {@link MicroDialogMessageRule}
 *
 * A {@link MicroDialogMessageRule} can evaluate if the belonging
 * {@link MicroDialogMessage} should be send. If all
 * {@link MicroDialogMessageRule}s return true a specific
 * {@link MicroDialogMessage} is send.
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
public class MicroDialogMessageRule extends AbstractRule {
	private static final long serialVersionUID = 4266506810684596781L;

	/**
	 * Default constructor
	 */
	public MicroDialogMessageRule(final ObjectId belongingMicroDialogMessage,
			final int order, final String ruleWithPlaceholders,
			final RuleEquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders,
			final String comment) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders, comment);

		this.belongingMicroDialogMessage = belongingMicroDialogMessage;
		this.order = order;
	}

	/**
	 * The {@link MicroDialogMessage} this rule belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	belongingMicroDialogMessage;

	/**
	 * The position of the {@link MicroDialogMessageRule} compared to all other
	 * {@link MicroDialogMessageRule}s; the first rule will be called first
	 */
	@Getter
	@Setter
	private int			order;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val microDialogMessage = new UIMicroDialogMessageRule(order,
				StringHelpers.createRuleName(this, true));

		microDialogMessage.setRelatedModelObject(this);

		return microDialogMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.mc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.AbstractSerializableTable#toTable()
	 */
	@Override
	@JsonIgnore
	public String toTable() {
		String table = wrapRow(wrapHeader("Rule:")
				+ wrapField(escape(StringHelpers.createRuleName(this, false))));
		table += wrapRow(
				wrapHeader("Comment:") + wrapField(escape(getComment())));

		return wrapTable(table);
	}
}
