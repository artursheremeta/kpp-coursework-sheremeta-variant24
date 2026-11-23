package ua.edu.nupp.kpp.client;

import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import ua.edu.nupp.kpp.client.model.*;
import ua.edu.nupp.kpp.client.net.HttpMuseumClient;
import ua.edu.nupp.kpp.client.task.ImportExhibitsTask;

/** Course project variant 24: four working CRUD tabs and CSV background import. */
public final class MainApp extends Application {
    private final HttpMuseumClient api=new HttpMuseumClient();
    private final TableView<Exhibit> exhibits=new TableView<>();
    private final TableView<Inspection> inspections=new TableView<>();
    private final TableView<Specialist> specialists=new TableView<>();
    private final TableView<ConservationRequest> requests=new TableView<>();
    private Stage stage;
    private Path csvPath=defaultCsv();

    public static void main(String[] args){launch(args);}
    @Override public void start(Stage stage) {
        this.stage=stage;
        TabPane tabs=new TabPane();tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(tab("Експонати",exhibitPane()),tab("Огляди",inspectionPane()),
                               tab("Заявки на консервацію",requestPane()),tab("Фахівці",specialistPane()));
        BorderPane root=new BorderPane(tabs);
        Label header=new Label("Контроль стану музейних експонатів | Варіант №24");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 16 16 10 16;");
        root.setTop(header);
        stage.setScene(new Scene(root,1240,750));
        stage.setTitle("Музейні експонати — курсова робота №24");stage.show();
        safe(this::refreshAll);
    }
    private static Tab tab(String title,javafx.scene.Node node){return new Tab(title,node);}
    private static <T> TableColumn<T,String> column(String label,Function<T,?> value) {
        TableColumn<T,String> c=new TableColumn<>(label);
        c.setCellValueFactory(cell->new ReadOnlyStringWrapper(String.valueOf(value.apply(cell.getValue()))));
        c.setPrefWidth(115);return c;
    }
    private static TextField field(GridPane grid,int row,String label,String initial) {
        Label caption=new Label(label);caption.setWrapText(true);
        TextField tf=new TextField(initial);tf.setPrefColumnCount(18);
        grid.add(caption,0,row);grid.add(tf,1,row);return tf;
    }
    private static GridPane grid(){
        GridPane grid=new GridPane();grid.setVgap(9);grid.setHgap(10);
        ColumnConstraints left=new ColumnConstraints(115);
        ColumnConstraints right=new ColumnConstraints(180);
        grid.getColumnConstraints().addAll(left,right);return grid;
    }
    private static Button button(String title,Runnable callback) {
        Button b=new Button(title);b.setOnAction(e->callback.run());return b;
    }
    private static VBox sidebar(String title,GridPane inputs,HBox... rows) {
        Label caption=new Label(title);caption.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        VBox v=new VBox(13);v.getChildren().addAll(caption,inputs);
        v.getChildren().addAll(rows);v.setPrefWidth(350);v.setPadding(new Insets(15));
        v.setStyle("-fx-background-color: #f5f7fa; -fx-border-color: #dfe4eb; -fx-border-width: 0 0 0 1;");
        return v;
    }
    private static BorderPane pane(TableView<?> table,VBox sidebar) {
        BorderPane p=new BorderPane();p.setCenter(table);p.setRight(sidebar);p.setPadding(new Insets(10));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Даних немає. Перевірте сервер або додайте запис."));return p;
    }
    private static long selectedId(long n) {
        if(n<=0)throw new IllegalArgumentException("Спочатку виберіть запис у таблиці");return n;
    }
    @FunctionalInterface private interface Action{void run() throws Exception;}
    private void safe(Action action) {
        try{action.run();}catch(Exception e){
            Alert a=new Alert(Alert.AlertType.ERROR);a.setTitle("Помилка");a.setHeaderText("Не вдалося виконати операцію");
            a.setContentText(e.getMessage()==null?e.toString():e.getMessage());a.showAndWait();
        }
    }
    private void info(String title,String message){
        Alert a=new Alert(Alert.AlertType.INFORMATION);a.setTitle(title);a.setHeaderText(null);
        a.setContentText(message);a.showAndWait();
    }
    private static int intOf(TextField f){return Integer.parseInt(f.getText().trim());}
    private static long longOf(TextField f){return Long.parseLong(f.getText().trim());}
    private static long optionalLong(TextField f){return f.getText().isBlank()?0:longOf(f);}
    private void refreshAll() throws Exception {refreshExhibits();refreshInspections();refreshRequests();refreshSpecialists();}
    private void refreshExhibits() throws Exception {exhibits.setItems(FXCollections.observableArrayList(api.getExhibits()));}
    private void refreshInspections() throws Exception {inspections.setItems(FXCollections.observableArrayList(api.getInspections()));}
    private void refreshRequests() throws Exception {requests.setItems(FXCollections.observableArrayList(api.getRequests()));}
    private void refreshSpecialists() throws Exception {specialists.setItems(FXCollections.observableArrayList(api.getSpecialists()));}

    private BorderPane exhibitPane() {
        exhibits.getColumns().addAll(column("ID",Exhibit::id),column("Інвентарний №",Exhibit::inventoryNumber),
          column("Назва",Exhibit::title),column("Матеріал",Exhibit::material),column("Місце",Exhibit::location),
          column("F1",Exhibit::sensitivityScore),column("F3",Exhibit::exhibitionScore));
        GridPane g=grid();
        TextField inv=field(g,0,"Інвентарний №",""),title=field(g,1,"Назва",""),
          material=field(g,2,"Матеріал",""),location=field(g,3,"Місце",""),
          f1=field(g,4,"F1 (0–100)","50"),f3=field(g,5,"F3 (0–100)","50");
        exhibits.getSelectionModel().selectedItemProperty().addListener((o,old,e)->{
            if(e==null)return;
            inv.setText(e.inventoryNumber());title.setText(e.title());material.setText(e.material());
            location.setText(e.location());f1.setText(""+e.sensitivityScore());f3.setText(""+e.exhibitionScore());
        });
        java.util.function.Supplier<Exhibit> read=()->new Exhibit(0,inv.getText(),title.getText(),material.getText(),
                location.getText(),intOf(f1),intOf(f3));
        HBox one=new HBox(7,
            button("Додати",()->safe(()->{api.addExhibit(read.get());refreshExhibits();})),
            button("Змінити",()->safe(()->{Exhibit s=exhibits.getSelectionModel().getSelectedItem();
                api.updateExhibit(selectedId(s==null?0:s.id()),read.get());refreshExhibits();})));
        HBox two=new HBox(7,
            button("Видалити",()->safe(()->{Exhibit s=exhibits.getSelectionModel().getSelectedItem();
                api.deleteExhibit(selectedId(s==null?0:s.id()));refreshAll();})),
            button("Оновити",()->safe(this::refreshExhibits)));
        BorderPane p=pane(exhibits,sidebar("Картка експоната",g,one,two));

        ProgressBar progress=new ProgressBar(0);progress.setPrefWidth(220);
        Label status=new Label("Файл: "+csvPath.getFileName());status.setMaxWidth(500);status.setWrapText(true);
        Button choose=new Button("Обрати CSV");
        choose.setOnAction(e->{FileChooser dialog=new FileChooser();dialog.setTitle("Каталог експонатів CSV");
            dialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV файли","*.csv"));
            java.io.File selected=dialog.showOpenDialog(stage);if(selected!=null){csvPath=selected.toPath();status.setText("Файл: "+csvPath);}
        });
        Button load=new Button("Імпортувати CSV");
        Button cancel=new Button("Скасувати");cancel.setDisable(true);
        load.setOnAction(ev->{
            ImportExhibitsTask task=new ImportExhibitsTask(csvPath,api);
            progress.progressProperty().bind(task.progressProperty());
            status.textProperty().bind(task.messageProperty());
            load.setDisable(true);choose.setDisable(true);cancel.setDisable(false);
            cancel.setOnAction(e->task.cancel());
            Runnable cleanup=()->{
                progress.progressProperty().unbind();status.textProperty().unbind();
                load.setDisable(false);choose.setDisable(false);cancel.setDisable(true);
            };
            task.setOnSucceeded(e->{cleanup.run();progress.setProgress(1);
                status.setText("Готово: додано "+task.getValue()+"; пропущено "+task.getSkipped());
                safe(this::refreshExhibits);
                String details=task.getErrors().isEmpty()?"":("\n\nПомилки (перші 15):\n"+String.join("\n",task.getErrors()));
                info("Результат імпорту","Додано: "+task.getValue()+"; пропущено: "+task.getSkipped()+details);
            });
            task.setOnFailed(e->{cleanup.run();status.setText("Помилка імпорту");
                safe(()->{throw new IllegalStateException(task.getException()==null?"Невідома помилка":task.getException().getMessage());});
            });
            task.setOnCancelled(e->{cleanup.run();status.setText("Імпорт скасовано (частину рядків уже могло бути додано)");
                safe(this::refreshExhibits);
            });
            Thread worker=new Thread(task,"csv-import");worker.setDaemon(true);worker.start();
        });
        HBox bottom=new HBox(12,new Label("Фоновий CSV-імпорт"),choose,load,cancel,progress,status);
        bottom.setPadding(new Insets(15,4,8,4));p.setBottom(bottom);return p;
    }
    private BorderPane inspectionPane() {
        inspections.getColumns().addAll(column("ID",Inspection::id),column("ID експоната",Inspection::exhibitId),
          column("Дата",Inspection::inspectionDate),column("F2",Inspection::damageScore),column("Результат огляду",Inspection::notes));
        GridPane g=grid();
        TextField exhibit=field(g,0,"ID експоната","1"),date=field(g,1,"Дата РРРР-ММ-ДД",LocalDate.now().toString()),
            damage=field(g,2,"F2 (0–100)","0"),notes=field(g,3,"Примітки","Огляд музейного предмета");
        inspections.getSelectionModel().selectedItemProperty().addListener((o,old,i)->{
            if(i!=null){exhibit.setText(""+i.exhibitId());date.setText(i.inspectionDate());
                damage.setText(""+i.damageScore());notes.setText(i.notes());}
        });
        java.util.function.Supplier<Inspection> read=()->new Inspection(0,longOf(exhibit),date.getText(),intOf(damage),notes.getText());
        HBox one=new HBox(7,
            button("Додати",()->safe(()->{api.addInspection(read.get());refreshInspections();})),
            button("Змінити",()->safe(()->{Inspection s=inspections.getSelectionModel().getSelectedItem();
                api.updateInspection(selectedId(s==null?0:s.id()),read.get());refreshInspections();})));
        HBox two=new HBox(7,
            button("Видалити",()->safe(()->{Inspection s=inspections.getSelectionModel().getSelectedItem();
                api.deleteInspection(selectedId(s==null?0:s.id()));refreshAll();})),
            button("Оновити",()->safe(this::refreshInspections)));
        return pane(inspections,sidebar("Результат огляду",g,one,two));
    }
    private BorderPane specialistPane() {
        specialists.getColumns().addAll(column("ID",Specialist::id),column("ПІБ",Specialist::fullName),
          column("Спеціалізація",Specialist::specialization),column("Контакт",Specialist::contact));
        GridPane g=grid();
        TextField name=field(g,0,"ПІБ",""),special=field(g,1,"Спеціалізація",""),contact=field(g,2,"Контакт"," ");
        specialists.getSelectionModel().selectedItemProperty().addListener((o,old,s)->{
            if(s!=null){name.setText(s.fullName());special.setText(s.specialization());contact.setText(s.contact());}
        });
        java.util.function.Supplier<Specialist> read=()->new Specialist(0,name.getText(),special.getText(),contact.getText());
        HBox one=new HBox(7,
            button("Додати",()->safe(()->{api.addSpecialist(read.get());refreshSpecialists();})),
            button("Змінити",()->safe(()->{Specialist s=specialists.getSelectionModel().getSelectedItem();
                api.updateSpecialist(selectedId(s==null?0:s.id()),read.get());refreshSpecialists();})));
        HBox two=new HBox(7,
            button("Видалити",()->safe(()->{Specialist s=specialists.getSelectionModel().getSelectedItem();
                api.deleteSpecialist(selectedId(s==null?0:s.id()));refreshAll();})),
            button("Оновити",()->safe(this::refreshSpecialists)));
        return pane(specialists,sidebar("Картка фахівця",g,one,two));
    }
    private static String stateUa(String s) {
        return switch(s){
            case "NEW" -> "НОВА";
            case "ASSESSED" -> "ОЦІНЕНО";
            case "IN_PROGRESS" -> "РОБОТИ";
            case "COMPLETED" -> "ЗАВЕРШЕНО";
            default -> s;
        };
    }
    private BorderPane requestPane() {
        requests.getColumns().addAll(column("ID",ConservationRequest::id),column("Експонат",ConservationRequest::exhibitId),
          column("Огляд",ConservationRequest::inspectionId),column("Фахівець",r->r.specialistId()==0?"—":r.specialistId()),
          column("Стан",r->stateUa(r.state())),column("F1",ConservationRequest::f1),column("F2",ConservationRequest::f2),
          column("F3",ConservationRequest::f3),column("I",r->String.format(Locale.ROOT,"%.2f",r.integralScore())));
        GridPane g=grid();
        TextField exhibit=field(g,0,"ID експоната","1"),inspection=field(g,1,"ID огляду","1"),
            specialist=field(g,2,"ID фахівця (або порожньо)","1");
        Label tip=new Label("Індекс I рахує сервер: F1 з експоната, F2 з огляду, F3 з експоната.");
        tip.setWrapText(true);g.add(tip,0,3,2,1);
        ComboBox<String> state=new ComboBox<>(FXCollections.observableArrayList("ASSESSED","IN_PROGRESS","COMPLETED"));
        state.setConverter(new StringConverter<>(){
            @Override public String toString(String s){return s==null?"":stateUa(s);}
            @Override public String fromString(String s){return s;}
        });
        state.getSelectionModel().selectFirst();
        requests.getSelectionModel().selectedItemProperty().addListener((o,old,r)->{
            if(r!=null){exhibit.setText(""+r.exhibitId());inspection.setText(""+r.inspectionId());
                specialist.setText(r.specialistId()==0?"":""+r.specialistId());
                state.setValue(switch(r.state()){
                    case "NEW" -> "ASSESSED"; case "ASSESSED" -> "IN_PROGRESS";
                    case "IN_PROGRESS" -> "COMPLETED"; default -> "COMPLETED";});
            }
        });
        java.util.function.Supplier<ConservationRequest> read=()->new ConservationRequest(0,longOf(exhibit),
                longOf(inspection),optionalLong(specialist),"NEW",0,0,0,0);
        HBox one=new HBox(7,
            button("Створити",()->safe(()->{api.addRequest(read.get());refreshRequests();})),
            button("Змінити",()->safe(()->{ConservationRequest s=requests.getSelectionModel().getSelectedItem();
                api.updateRequest(selectedId(s==null?0:s.id()),read.get());refreshRequests();})));
        HBox two=new HBox(7,
            button("Видалити",()->safe(()->{ConservationRequest s=requests.getSelectionModel().getSelectedItem();
                api.deleteRequest(selectedId(s==null?0:s.id()));refreshRequests();})),
            button("Оновити",()->safe(this::refreshRequests)));
        HBox move=new HBox(7,state,button("Змінити стан",()->safe(()->{
            ConservationRequest s=requests.getSelectionModel().getSelectedItem();
            api.changeRequestState(selectedId(s==null?0:s.id()),state.getValue());refreshRequests();
        })));
        return pane(requests,sidebar("Заявка на консервацію",g,one,two,move));
    }
    private static Path defaultCsv(){
        Path local=Path.of("sample-data","exhibits.csv");
        if(Files.exists(local))return local;
        Path fromRoot=Path.of("client","sample-data","exhibits.csv");
        return Files.exists(fromRoot)?fromRoot:local;
    }
}
