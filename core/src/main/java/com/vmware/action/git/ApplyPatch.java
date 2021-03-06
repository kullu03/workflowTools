package com.vmware.action.git;

import com.vmware.action.base.BaseCommitAction;
import com.vmware.config.ActionAfterFailedPatchCheck;
import com.vmware.config.ActionDescription;
import com.vmware.config.WorkflowConfig;
import com.vmware.reviewboard.domain.RepoType;
import com.vmware.util.scm.FileChange;
import com.vmware.util.scm.Perforce;
import com.vmware.util.scm.diff.DiffConverter;
import com.vmware.util.scm.diff.GitDiffToPerforceConverter;
import com.vmware.util.scm.diff.PerforceDiffToGitConverter;
import com.vmware.util.CommandLineUtils;
import com.vmware.util.IOUtils;
import com.vmware.util.StringUtils;
import com.vmware.util.logging.LogLevel;
import com.vmware.util.logging.Padder;

import java.io.File;
import java.util.List;

@ActionDescription("Used to apply patch data.")
public class ApplyPatch extends BaseCommitAction {

    public ApplyPatch(WorkflowConfig config) {
        super(config);
    }

    @Override
    public String failWorkflowIfConditionNotMet() {
        if (StringUtils.isBlank(draft.draftPatchData)) {
            return "no patch data loaded";
        }
        return super.failWorkflowIfConditionNotMet();
    }

    @Override
    public void process() {
        String patchData = draft.draftPatchData;
        RepoType repoType = draft.repoType;

        List<FileChange> fileChanges = null;
        boolean isPerforceClient = !git.workingDirectoryIsInGitRepo();
        if (isPerforceClient) {
            exitIfPerforceClientCannotBeUsed();
        }

        DiffConverter diffConverter = null;
        if (repoType == RepoType.perforce) {
            diffConverter = new PerforceDiffToGitConverter(git);
            patchData = diffConverter.convert(patchData);
        } else if (isPerforceClient){
            diffConverter = new GitDiffToPerforceConverter(getLoggedInPerforceClient(), "");
        }

        if (diffConverter != null) {
            fileChanges = diffConverter.getFileChanges();
        }

        String changelistId = null;
        if (isPerforceClient) {
            changelistId = determineChangelistIdToUse();
            Perforce perforce = getLoggedInPerforceClient();
            perforce.syncPerforceFiles(fileChanges, "");
            perforce.openFilesForEditIfNeeded(changelistId, fileChanges);
        }
        log.trace("Patch to apply\n{}", patchData);

        File patchFile = new File(System.getProperty("user.dir") + File.separator + "workflow.patch");
        patchFile.delete();
        IOUtils.write(patchFile, patchData);

        PatchCheckResult patchCheckResult = checkIfPatchApplies(patchData);
        String result = applyPatch(patchData, patchCheckResult);

        if (StringUtils.isBlank(result.trim())) {
            log.info("Patch successfully applied, patch is stored in workflow.patch");
        } else {
            printPatchResult(result);
        }
        if (isPerforceClient) {
            serviceLocator.getPerforce().renameAddOrDeleteFiles(changelistId, fileChanges);
        }
    }

    protected String applyPatch(String patchData, PatchCheckResult patchCheckResult) {
        log.info("Applying patch");
        String result = "";
        switch (patchCheckResult) {
            case applyPatchUsingGitApply:
                result = git.applyPatch(patchData, false);
                break;
            case applyPartialPatchUsingGitApply:
                result = git.applyPartialPatch(patchData);
                break;
            case applyPartialPatchUsingPatchCommand:
            case applyPatchUsingPatchCommand:
                result = patch(patchData, false);
                break;
            default:
                exitWithWarnMessage("Not applying patch, patch is stored in workflow.patch");
         }
        return result;
    }

    private void exitIfPerforceClientCannotBeUsed() {
        String perforceClientCannotBeUsed = perforceClientCannotBeUsed();
        if (perforceClientCannotBeUsed != null) {
            throw new RuntimeException(perforceClientCannotBeUsed);
        }
    }

    private PatchCheckResult checkIfPatchApplies(String patchData) {
        log.info("Checking if patch applies");
        String checkResult = patchConfig.usePatchCommand ? patch(patchData, true) : git.applyPatch(patchData, true);
        String checkCommand = patchConfig.usePatchCommand ? patchConfig.patchCommand + " --dry-run"
                : "git apply --ignore-whitespace -3 --check";
        if (StringUtils.isBlank(checkResult)) {
            return patchConfig.usePatchCommand ? PatchCheckResult.applyPatchUsingPatchCommand :
                    PatchCheckResult.applyPatchUsingGitApply;
        }

        printCheckOutput(checkResult, checkCommand);
        if (patchConfig.actionAfterFailedPatchCheck == null) {
            patchConfig.actionAfterFailedPatchCheck = ActionAfterFailedPatchCheck.askForAction(patchConfig.usePatchCommand);
        }

        switch (patchConfig.actionAfterFailedPatchCheck) {
            case partial:
                return patchConfig.usePatchCommand ? PatchCheckResult.applyPartialPatchUsingPatchCommand :
                        PatchCheckResult.applyPatchUsingGitApply;
            case usePatchCommand:
                if (patchConfig.usePatchCommand) {
                    return PatchCheckResult.nothing;
                }
                patchConfig.usePatchCommand = true;
                return checkIfPatchApplies(patchData);
            default:
                return PatchCheckResult.nothing;
        }
    }

    private void printCheckOutput(String output, String title) {
        Padder padder = new Padder(title);
        padder.errorTitle();
        log.info(output);
        padder.errorTitle();
        log.error("Checking of patch failed!");
    }

    private void printPatchResult(String result) {
        log.warn("Potential issues with applying patch, patch is stored in workflow.patch");
        Padder padder = new Padder("Patch Output");
        padder.warnTitle();
        log.info(result);
        padder.warnTitle();
    }

    private String patch(String patchData, boolean dryRun) {
        String command = patchConfig.patchCommand;
        if (dryRun) {
            command += " --dry-run";
        }
        LogLevel logLevel = dryRun ? LogLevel.DEBUG : LogLevel.INFO;
        return CommandLineUtils.executeCommand(null, command, patchData, logLevel);
    }

    private enum PatchCheckResult {
        nothing,
        applyPartialPatchUsingGitApply,
        applyPartialPatchUsingPatchCommand,
        applyPatchUsingGitApply,
        applyPatchUsingPatchCommand;
    }
}
