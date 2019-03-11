package ch.ethz.mc.tools;

/* ##LICENSE## */
import java.util.ArrayList;
import java.util.List;

import ch.ethz.mc.model.persistent.MicroDialogDecisionPoint;
import ch.ethz.mc.model.persistent.MicroDialogMessage;
import ch.ethz.mc.model.persistent.concepts.MicroDialogElementInterface;
import lombok.val;

/**
 * Merges lists of several types
 *
 * @author Andreas Filler
 */
public class ListMerger {
	public static List<MicroDialogElementInterface> mergeMicroDialogElements(
			final Iterable<MicroDialogMessage> iterableA,
			final Iterable<MicroDialogDecisionPoint> iterableB) {

		val sortedList = new ArrayList<MicroDialogElementInterface>();

		val iteratorA = iterableA.iterator();
		val iteratorB = iterableB.iterator();
		MicroDialogElementInterface rememberedElementA = null;
		MicroDialogElementInterface rememberedElementB = null;
		while (rememberedElementA != null || rememberedElementB != null
				|| iteratorA.hasNext() || iteratorB.hasNext()) {
			if (rememberedElementA == null && iteratorA.hasNext()) {
				rememberedElementA = iteratorA.next();
			}
			if (rememberedElementB == null && iteratorB.hasNext()) {
				rememberedElementB = iteratorB.next();
			}
			if (rememberedElementA == null) {
				sortedList.add(rememberedElementB);
				rememberedElementB = null;
			} else if (rememberedElementB == null) {
				sortedList.add(rememberedElementA);
				rememberedElementA = null;
			} else {
				if (rememberedElementA.getOrder() < rememberedElementB
						.getOrder()) {
					sortedList.add(rememberedElementA);
					rememberedElementA = null;
				} else if (rememberedElementA.getOrder() > rememberedElementB
						.getOrder()) {
					sortedList.add(rememberedElementB);
					rememberedElementB = null;
				} else {
					sortedList.add(rememberedElementA);
					sortedList.add(rememberedElementB);
					rememberedElementA = null;
					rememberedElementB = null;
				}
			}
		}

		return sortedList;
	}
}
