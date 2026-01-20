package harmonised.pmmo.api.client.types;

import java.util.Collection;

@FunctionalInterface
public interface GlossaryFilter {
    /**Widgets that implement this class receive a record of filter
     * values to operate over as it makes sense for their content.
     * If the result of the implementing class's filter operation
     * results in the entire object being filtered, this method will
     * return true to let parent classes know the child is filtered.
     *
     * For example, a class that implements display values for enums
     * of REQ and XP would still display the opposite value if filtered
     * by either, but only if both exist.  whereas if the filter was
     * for a skill and neither REQ nor XP contained the specified skill
     * the implementation would return true since there is no content
     * remaining after the filter is applied
     *
     * @param filter a record of filterable values
     * @return true if this object displays no content after the filter is applied.
     */
    boolean applyFilter(Filter filter);

    static class Filter {
        private String textFilter = "";
        private SELECTION selection = null;
        private OBJECT objectType = null;
        private String skill = "";
        private GuiEnumGroup enumGroup = null;

        public Filter(String textFilter) {this.textFilter = textFilter;}
        public Filter(String textFilter, SELECTION selection) {this.textFilter = textFilter; this.selection = selection;}
        public Filter(String textFilter, OBJECT objectTYpe) {this.textFilter = textFilter; this.objectType = objectTYpe;}
        public Filter(String textFilter, GuiEnumGroup group) {this.textFilter = textFilter; this.enumGroup = group;}
        public Filter(String textFilter, String skill) {this.textFilter = textFilter; this.skill = skill;}

        public Filter with(SELECTION selection) {this.selection = selection; return this;}
        public Filter with(OBJECT objectType) {this.objectType = objectType; return this;}
        public Filter with(GuiEnumGroup group) {this.enumGroup = group; return this;}
        public Filter with(String skill) {this.skill = skill; return this;}

        public String getTextFilter() {return textFilter;}
        public SELECTION getSelection() {return selection;}
        public OBJECT getObjectType() {return objectType; }
        public String getSkill() {return skill;}
        public GuiEnumGroup getEnumGroup() {return enumGroup;}

        public boolean matchesTextFilter(String str) {
            return textFilter.isEmpty() || str.contains(textFilter);
        }
        public boolean matchesSkill(Collection<String> skills) {return skill.isEmpty() || skills.contains(skill);}
        public boolean matchesSkill(String skillMatch) {return skill.isEmpty() || skill.equals(skillMatch);}

        public boolean matchesObject(OBJECT obj) {
            return objectType == null || objectType == OBJECT.NONE || objectType == obj;
        }

        public boolean matchesSelection(SELECTION sel) {
            return selection == null || selection == SELECTION.NONE || selection == sel;
        }

        public boolean matchesEnum(GuiEnumGroup value) {
            return enumGroup == null || enumGroup == value;
        }
        public boolean matchesEnum(Collection<GuiEnumGroup> value) {
            return enumGroup == null || value.contains(enumGroup);
        }
    }
}
