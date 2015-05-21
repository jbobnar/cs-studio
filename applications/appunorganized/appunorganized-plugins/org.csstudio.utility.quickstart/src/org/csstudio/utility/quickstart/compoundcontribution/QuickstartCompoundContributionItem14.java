package org.csstudio.utility.quickstart.compoundcontribution;

import org.eclipse.jface.action.IContributionItem;

public class QuickstartCompoundContributionItem14 extends
        AbstractQuickstartCompoundContributionItem {

    /**
     * ID for the command.
     */
    private final String commandID = "org.csstudio.utility.quickstart.command14";

    /**
     * ID for the compound.
     */
    private final String compoundID = "org.csstudio.utility.quickstart.QuickstartCompoundContributionItem14";

    public QuickstartCompoundContributionItem14() {
    }

    public QuickstartCompoundContributionItem14(String id) {
        super(id);
    }

    @Override
    protected IContributionItem[] getContributionItems() {
        return getItemsForMenuNo(14, commandID, compoundID);
    }
}
