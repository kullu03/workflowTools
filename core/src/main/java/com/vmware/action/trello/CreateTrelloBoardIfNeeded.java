package com.vmware.action.trello;

import com.vmware.action.base.AbstractTrelloAction;
import com.vmware.config.ActionDescription;
import com.vmware.config.WorkflowConfig;
import com.vmware.trello.domain.Board;
import com.vmware.utils.InputUtils;
import com.vmware.utils.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

@ActionDescription("Creates a trello board if the project name doesn't match an open trello board.")
public class CreateTrelloBoardIfNeeded extends AbstractTrelloAction {


    public CreateTrelloBoardIfNeeded(WorkflowConfig config) {
        super(config);
    }

    @Override
    public void process() throws IOException, IllegalAccessException, URISyntaxException, ParseException {
        String boardName = projectIssues.projectName;

        if (StringUtils.isBlank(boardName)) {
            boardName = InputUtils.readValueUntilNotBlank("Enter trello board name");
        }

        Board[] openBoards = trello.getOpenBoardsForUser();
        List<Board> existingBoards = Arrays.asList(openBoards);
        int boardIndex = existingBoards.indexOf(boardName);
        if (boardIndex != -1) {
            log.info("Found matching trello board {}", boardName);
            selectedBoard.readValues(existingBoards.get(boardIndex));
        } else {
            log.info("No matching trello board found, using name {} for new board", boardName);
            createTrelloBoard(boardName);
        }



    }
}
