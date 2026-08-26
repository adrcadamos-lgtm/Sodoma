package pe.edu.upeu.poo3;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;



public class Matriz extends Application {
    Label informacion;
    GridPane grid;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Label titulo=new Label("MATRIZ - FORMAS");

        Label lbnTm=new Label("Tamaño de la Matriz:");
        Spinner<Integer> spTm=new Spinner<>(2, 30, 5);

        Label lbnNumI=new Label("Numero de Inicio:");
        Spinner<Integer> spNumI=new Spinner<>(0, 30, 0);
        Button btnGenerar=new Button("Generar Matriz");
        informacion=new Label("Mostrar Posiciones....!");

        grid=new GridPane();
        grid.setVgap(3);
        grid.setHgap(3);
        btnGenerar.setOnAction(event -> {
            matrisF5(spTm.getValue(), spNumI.getValue());
        });
        ChoiceBox<String> formas = new ChoiceBox<>();
        formas.getItems().addAll("Forma 5", "Forma 11", "Forma 13", "Forma X");
        formas.setValue("Forma 5");
        Label lbnFormas=new Label("Elegir Froma:");
        HBox hbx=new HBox(15, lbnFormas, formas);
        HBox hb=new HBox(10, lbnTm, spTm,lbnNumI, spNumI, btnGenerar );
        matrisF5(spTm.getValue(), spNumI.getValue());
        VBox vb=new VBox(15, titulo, hb,hbx, grid, informacion);
        ScrollPane cp=new ScrollPane(vb);
        primaryStage.setScene(new Scene(cp));
        primaryStage.setTitle("Formas Matriciales");
        primaryStage.show();
    }

    public void matrisF5(int tamaho, int numI){
        for (int t=0; t < tamaho; t++){
            for(int j= tamaho-1; j >= tamaho-1-t; j-- ){
                Button cuadro=new Button(String.valueOf(numI));
                cuadro.setMinSize(48,42);
                cuadro.setPrefSize(48,42);
                grid.add(cuadro,j,t); numI++;
            }
        }
    }
    public static void Forma5(int[][] m){
            for (int i = 0; i < m.length; i++) {
                for (int j = 0; j < m[1].length; j++) {
                    if(m[i][j]!=0){
                        System.out.print(m[i][j]+"\t");
                    }else{
                        System.out.print(" \t");
                    }
                }
                System.out.println("");
            }
        }
}

