package UnemployedVoodooFamily.Logic;

import UnemployedVoodooFamily.Data.ProjectModel;
import UnemployedVoodooFamily.GUI.Content.ValueFactory;
import ch.simas.jtoggl.TimeEntry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProjectSummaryLogic {
    private int year = - 1;
    private final Map<String, ProjectModel> projects = new HashMap<>();

    public void setSelectedYear(int year) {
        this.year = year;
    }

    public List<ProjectModel> buildMasterData(List<TimeEntry> timeEntries) {
        buildProjectMonthTotals(timeEntries);
        return generateSummaryList();
    }

    private void buildProjectMonthTotals(List<TimeEntry> timeEntries) {
        projects.clear();
        for(TimeEntry te: timeEntries) {
            if(te.getStart().getYear() == year && te.getProject() != null) {
                String projectName = te.getProject().getName();
                String month = ValueFactory.formatMonthName(te.getStart().getMonth());
                ProjectModel projectData = projects.get(projectName);
                if(projectData == null) {
                    // First entry for the project
                    projectData = new ProjectModel(projectName);
                    projects.put(projectName, projectData);
                }
                projectData.addHours(month, (float) te.getDuration() / 3600.0);
            }
        }
    }

    /**
     * Generate a list containing project summary data
     * @return A list with accumulated project summaries
     */
    private List<ProjectModel> generateSummaryList() {
        return new LinkedList<>(projects.values());
    }
}
