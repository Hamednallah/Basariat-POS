package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher; // For mocking static service getter
import com.basariatpos.model.BankNameDTO;
import com.basariatpos.service.BankNameService;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;


@ExtendWith(ApplicationExtension.class)
class BankNameManagementControllerTest {

    @Mock
    private BankNameService mockBankNameService;

    private BankNameManagementController controller;
    private Parent root;
    private Stage stage;
    private MockedStatic<AppLauncher> appLauncherMockedStatic;

    @Start
    private void start(Stage stage) throws IOException {
        MockitoAnnotations.openMocks(this);
        this.stage = stage;

        appLauncherMockedStatic = Mockito.mockStatic(AppLauncher.class);
        appLauncherMockedStatic.when(AppLauncher::getBankNameService).thenReturn(mockBankNameService);

        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE);
        ResourceBundle bundle = MessageProvider.getBundle();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/BankNameManagementView.fxml"));
        loader.setResources(bundle);

        root = loader.load();
        controller = loader.getController();
        // Service is set in initialize via AppLauncher.getBankNameService()
        // If direct setter was preferred: controller.setBankNameService(mockBankNameService);

        Scene scene = new Scene(root, 700, 500);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp() throws Exception {
        BankNameDTO bank1 = new BankNameDTO(1, "Bank Al Khartoum", "بنك الخرطوم", true);
        BankNameDTO bank2 = new BankNameDTO(2, "Faisal Islamic Bank", "بنك فيصل الإسلامي", true);
        when(mockBankNameService.getAllBankNames(true)).thenReturn(Arrays.asList(bank1, bank2));

        // Manually trigger reload if initialize doesn't pick up the latest mock state correctly
        // or if service is set late. Here, initialize should get the mock via AppLauncher.
        // If controller's initialize calls loadBankNames, this should be enough.
        // controller.loadBankNames(); // Not usually needed if initialize does its job
    }

    @AfterEach
    void tearDown(FxRobot robot) {
        if (appLauncherMockedStatic != null) {
            appLauncherMockedStatic.close();
        }
        // Close any dialogs opened
        List<Window> allWindows = new ArrayList<>(robot.listWindows());
        allWindows.stream()
            .filter(window -> window instanceof Stage && window != stage)
            .forEach(window -> robot.targetWindow(window).interact(Stage::close));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void tableIsPopulated_onInitialize(FxRobot robot) {
        TableView<BankNameDTO> bankNamesTable = robot.lookup("#bankNamesTable").queryTableView();
        assertNotNull(bankNamesTable);
        // Wait for items to be potentially loaded asynchronously
        robot.waitUntil(() -> bankNamesTable.getItems().size() == 2, 2000); // Wait up to 2 seconds
        assertEquals(2, bankNamesTable.getItems().size(), "Table should have two bank names.");

        ObservableList<BankNameDTO> items = bankNamesTable.getItems();
        assertTrue(items.stream().anyMatch(b -> b.getBankNameEn().equals("Bank Al Khartoum")));
        assertTrue(items.stream().anyMatch(b -> b.getBankNameAr().equals("بنك فيصل الإسلامي")));
    }

    @Test
    void addButton_opensBankNameFormDialog(FxRobot robot) {
        robot.clickOn("#addButton");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify dialog is shown
        List<Window> windows = robot.listWindows();
        assertTrue(windows.stream().anyMatch(w -> w instanceof Stage &&
                                              ((Stage)w).getTitle().equals(MessageProvider.getString("bankname.dialog.add.title"))),
                   "Add Bank Name dialog should open.");

        // Close the dialog for cleanup
         windows.stream()
            .filter(w -> w instanceof Stage && ((Stage)w).getTitle().equals(MessageProvider.getString("bankname.dialog.add.title")))
            .findFirst().ifPresent(w -> robot.targetWindow(w).close());
    }

    @Test
    void editButton_enablesAndOpensDialog_onSelection(FxRobot robot) {
        Button editButton = robot.lookup("#editButton").queryButton();
        assertTrue(editButton.isDisabled(), "Edit button should be disabled initially.");

        TableView<BankNameDTO> bankNamesTable = robot.lookup("#bankNamesTable").queryTableView();
        robot.waitUntil(() -> bankNamesTable.getItems().size() > 0, 1000);

        robot.interact(() -> bankNamesTable.getSelectionModel().selectFirst());
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(editButton.isDisabled(), "Edit button should be enabled after selection.");

        robot.clickOn(editButton);
        WaitForAsyncUtils.waitForFxEvents();

        List<Window> windows = robot.listWindows();
        assertTrue(windows.stream().anyMatch(w -> w instanceof Stage &&
                                              ((Stage)w).getTitle().equals(MessageProvider.getString("bankname.dialog.edit.title"))),
                   "Edit Bank Name dialog should open.");

        windows.stream()
            .filter(w -> w instanceof Stage && ((Stage)w).getTitle().equals(MessageProvider.getString("bankname.dialog.edit.title")))
            .findFirst().ifPresent(w -> robot.targetWindow(w).close());
    }

    @Test
    void toggleActiveButton_showsConfirmation_andCallsService(FxRobot robot) throws Exception {
        TableView<BankNameDTO> bankNamesTable = robot.lookup("#bankNamesTable").queryTableView();
        robot.waitUntil(() -> bankNamesTable.getItems().size() > 0, 1000);
        robot.interact(() -> bankNamesTable.getSelectionModel().selectFirst());
        WaitForAsyncUtils.waitForFxEvents();

        // Mock the service call for toggle
        doNothing().when(mockBankNameService).toggleBankNameStatus(anyInt());
        // Mock a new list to be returned after status toggle for table refresh
        BankNameDTO toggledBank = new BankNameDTO(1, "Bank Al Khartoum", "بنك الخرطوم", false); // Now inactive
        when(mockBankNameService.getAllBankNames(true)).thenReturn(Arrays.asList(toggledBank,
            new BankNameDTO(2, "Faisal Islamic Bank", "بنك فيصل الإسلامي", true)));


        robot.clickOn("#toggleActiveButton");
        WaitForAsyncUtils.waitForFxEvents(); // Wait for confirmation dialog

        // Confirm the dialog
        // robot.lookup(".alert.confirmation .button").match((Button b) -> b.getText().equals("Yes") || b.getText().equals(MessageProvider.getString("button.yes"))).queryButton();
        // For standard JavaFX dialogs, ButtonType.YES is often enough if it's the default action for Enter.
        // Or find button by text:
        List<Window> windows = robot.listWindows();
        Stage confirmationDialog = windows.stream()
            .filter(w -> w instanceof Stage && ((Stage)w).getTitle().equals(MessageProvider.getString("bankname.confirm.toggleActive.title")))
            .map(w -> (Stage)w)
            .findFirst().orElse(null);

        assertNotNull(confirmationDialog, "Confirmation dialog should be shown.");

        // Click "Yes" button on confirmation dialog
        // This requires knowing the text of the "Yes" button, which might be localized.
        // A more robust way is to find the button associated with ButtonType.YES.
        FxRobot dialogRobot = robot.targetWindow(confirmationDialog);
        dialogRobot.clickOn((Node)dialogRobot.lookup(".button").match((Button b) -> b.isDefaultButton() || b.getText().equalsIgnoreCase("yes")).query());
        WaitForAsyncUtils.waitForFxEvents();


        verify(mockBankNameService).toggleBankNameStatus(1); // ID of "Bank Al Khartoum"
        verify(mockBankNameService, times(2)).getAllBankNames(true); // Initial load + refresh

        // Check if table reflects the change (e.g., status column text)
        // This requires statusColumn's cellValueFactory to be re-evaluated
        // For simplicity, check if number of items is still same, assuming DTO object reference might change.
        assertEquals(2, bankNamesTable.getItems().size());
    }

    // Further tests:
    // - Error dialogs when service calls fail.
    // - Correct data passed to dialogs on edit.
    // - Table refresh logic after successful save/edit/toggle.
}
