package view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import model.Member;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import controller.RatingSystem;

/**
 * a class to call the initial log in or sign up window
 * 
 * @author Stephen
 *
 */
public class LogInController implements Initializable
{	
	@FXML
	private static RatingSystem r = new RatingSystem();

	@FXML
	private TextField userName = new TextField();

	@FXML
	private PasswordField passwordEntry = new PasswordField();

	@FXML
	private TextField firstName = new TextField();

	@FXML
	private TextField secondName = new TextField();

	@FXML
	private TextField desiredUserName = new TextField();

	@FXML
	private PasswordField desiredPassword = new PasswordField();

	@FXML
	private PasswordField reEnteredPassword = new PasswordField();

	@FXML
	private ChoiceBox<String> genrePreference = new ChoiceBox<String>();

	@FXML
	private TextField yearPreference = new TextField();

	private final ObservableList<String> list = FXCollections.observableArrayList(
			"Drama","Action","Comedy","Family","Horror","Crime","Biography","Adventure"
			);


	@FXML
	private ChoiceBox<String> rateFilm = new ChoiceBox<String>();

	@FXML
	private Label incorrectUser = new Label();

	@FXML
	private Label noSignUp = new Label();

	@FXML
	private Label incorrectDetails = new Label();

	private int loggedInIndex;
	
	@FXML
	private void expandGenres(MouseEvent event)
	{
		genrePreference.show();
	}

	@FXML
	private void signIn(MouseEvent event) throws IOException 
	{
		Member member = null;
		for(int i = 0; i<r.getMembers().size();i++)
		{
			if(r.getMembers().get(i).getAccountName().equals(userName.getText()))
			{
				member = r.getMembers().get(i);
				break;
			}
		}
		if(member!= null && member.getPassword().equals(passwordEntry.getText()))
		{
			r.logIn(userName.getText(), passwordEntry.getText());
			loggedInIndex = r.getMembers().indexOf(member);
			((Node)(event.getSource())).getScene().getWindow().hide();
			startUp();
		}
		else
		{
			incorrectUser.setText("Incorrect username or password. Try again.");
		}
	}


	@FXML
	private void signUp(MouseEvent event) throws IOException 
	{
		if(desiredPassword.getText().equals(reEnteredPassword.getText()) && Integer.valueOf(yearPreference.getText()) instanceof Integer)
		{
			Member member = r.newMember(firstName.getText(), secondName.getText(), desiredUserName.getText(), desiredPassword.getText(), genrePreference.getValue(), Integer.valueOf(yearPreference.getText()));
			loggedInIndex = r.getMembers().indexOf(member);
			r.saveMembers();
			((Node)(event.getSource())).getScene().getWindow().hide();
			startUp();
		}
		else if (firstName.getText().equals(null) 
				|| secondName.getText().equals(null) 
				|| desiredUserName.getText().equals(null)
				|| desiredPassword.getText().equals(null) 
				|| !(Integer.valueOf(yearPreference.getText()) instanceof Integer))
		{
			noSignUp.setText("Please complete all fields.");
		}
		else if(!desiredPassword.getText().equals(reEnteredPassword.getText()))
		{
			noSignUp.setText("Passwords do not match. Try again.");
		}
	}

	private void startUp() throws IOException
	{
		SystemController systemController = 
			    new SystemController(this.loggedInIndex);

			FXMLLoader loader = new FXMLLoader(
			    getClass().getResource(
			        "MainWindow.fxml"
			    )
			);
			loader.setController(systemController);

		
		Stage mainStage = new Stage();
		mainStage.setTitle("Film Recommender");
		Parent root = (Parent) loader.load();
		Scene scene = new Scene(root);
		mainStage.setScene(scene);
		mainStage.show();
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		genrePreference.setItems(list);
		genrePreference.setValue(list.get(0));
		r.loadFilms();
		r.loadMembers();
	}
}
