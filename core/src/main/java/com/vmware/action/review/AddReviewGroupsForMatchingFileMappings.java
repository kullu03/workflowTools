package com.vmware.action.review;

import java.util.List;

import com.vmware.action.base.BaseCommitAction;
import com.vmware.config.ActionDescription;
import com.vmware.config.WorkflowConfig;
import com.vmware.util.scm.FileChange;

@ActionDescription("Adds review groups to the commit if they match a file mapping.")
public class AddReviewGroupsForMatchingFileMappings extends BaseCommitAction{

    public AddReviewGroupsForMatchingFileMappings(WorkflowConfig config) {
        super(config);
    }

    @Override
    public String cannotRunAction() {
        if (reviewBoardConfig.reviewGroupFileMappings.isEmpty()) {
            return "no review group file mappings present";
        }
        return super.cannotRunAction();
    }

    @Override
    public void process() {
        List<FileChange> fileChanges = getFileChangesInLastCommit();

        for (String fileMapping : reviewBoardConfig.reviewGroupFileMappings.keySet()) {
            String matchingGroup = reviewBoardConfig.reviewGroupFileMappings.get(fileMapping);
            for (FileChange fileChange : fileChanges) {
                if (fileChange.getFirstFileAffected().startsWith(fileMapping)) {
                    addTargetGroupForPath(matchingGroup, fileChange.getFirstFileAffected(), fileMapping);
                } else if (fileChange.getLastFileAffected().startsWith(fileMapping)) {
                    addTargetGroupForPath(matchingGroup, fileChange.getLastFileAffected(), fileMapping);
                }
            }
        }
    }

    private void addTargetGroupForPath(String groupName, String path, String mapping) {
        if (draft.extraTargetGroupsToAdd.contains(groupName)) {
            return;
        }
        log.info("Adding review group {} as path {} matches mapping {}", groupName, path, mapping);
        draft.extraTargetGroupsToAdd.add(groupName);
    }
}
