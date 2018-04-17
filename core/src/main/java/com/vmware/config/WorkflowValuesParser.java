package com.vmware.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.action.BaseAction;
import com.vmware.util.StringUtils;

/**
 * Used to parse actions and config values from workflow arguments.
 */
public class WorkflowValuesParser {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private List<Class<? extends BaseAction>> actionClasses = new ArrayList<Class<? extends BaseAction>>();
    private Map<String, String> configValues = new HashMap<String, String>();
    private List<String> unknownActions = new ArrayList<String>();

    private Map<String, List<String>> workflows;
    private List<Class<? extends BaseAction>> workflowActions;

    public WorkflowValuesParser(Map<String, List<String>> workflows, List<Class<? extends BaseAction>> workflowActions) {
        this.workflows = workflows;
        this.workflowActions = workflowActions;
    }

    public void reset() {
        actionClasses.clear();
        configValues.clear();
        unknownActions.clear();
    }

    public void parse(List<String> workflowValues) {
        for (String workflowValue : workflowValues) {
            if (workflowValue.startsWith("-")) {
                int equalsIndex = workflowValue.indexOf("=");
                String[] configPieces = workflowValue.split("=");

                // assuming that the config value is boolean if no value specified
                String fieldValue = equalsIndex == -1 ? Boolean.TRUE.toString() : workflowValue.substring(equalsIndex + 1);
                configValues.put(configPieces[0], fieldValue);
                continue;
            }

            if (workflows.containsKey(workflowValue)) {
                parse(workflows.get(workflowValue));
                continue;
            }
            Optional<Class<? extends BaseAction>> matchingAction = workflowActions.stream().filter(action -> action.getSimpleName().equals(workflowValue)).findFirst();
            if (matchingAction.isPresent()) {
                actionClasses.add(matchingAction.get());
                log.trace("Found action class {}", workflowValue);
            } else {
                unknownActions.add(workflowValue);
            }
        }
    }

    public List<Class<? extends BaseAction>> getActionClasses() {
        return actionClasses;
    }

    public Map<String, String> getConfigValues() {
        return configValues;
    }

    public List<String> getUnknownActions() {
        return unknownActions;
    }

    public Collection<? extends String> calculateJenkinsParameterConfigValues() {
        String jenkinsJobsToCall = configValues.get("-j");
        if (StringUtils.isBlank(jenkinsJobsToCall)) {
            jenkinsJobsToCall = configValues.get("--jenkins-jobs");
        }
        if (StringUtils.isBlank(jenkinsJobsToCall)) {
            return Collections.emptyList();
        }
        String[] jenkinsJobPieces = jenkinsJobsToCall.split("&");
        if (jenkinsJobPieces.length < 2) {
            return Collections.emptyList();
        }
        Set<String> jenkinsParameterConfigValues = new HashSet<>();
        for (int i = 1; i < jenkinsJobPieces.length; i++) {
            String jenkinsParameter = jenkinsJobPieces[i];
            String[] jenkinsParameterPieces = jenkinsParameter.split("=");
            if (jenkinsParameterPieces.length != 2) {
                continue;
            }
            jenkinsParameterConfigValues.add("--J" + jenkinsParameterPieces[0]);
        }
        return jenkinsParameterConfigValues;
    }
}
