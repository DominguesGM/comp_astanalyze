package test;

import analyser.Analyzer;
import data.AST;
import other.Log;

import org.json.simple.JSONObject;
import output.Output;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Test {
    private static ArrayList<String> filesList = new ArrayList<>();
    private static AST ast;
    private static Analyzer analyzer;
    private static Output output;

    public static void main(String args[]){
        listFilesForFolder(new File("testsuite"));
        getAllGraphs();
    }

    private static ArrayList<String> listFilesForFolder(final File folder) {
        if(folder.exists() && folder.isDirectory()){
            for (final File fileEntry : folder.listFiles()) {
                String extension = "";

                int i = fileEntry.getName().lastIndexOf('.');
                if (i > 0) {
                    extension = fileEntry.getName().substring(i+1);
                }
                if (extension.equals("json")) {
                    filesList.add(fileEntry.getParent() + "/" + fileEntry.getName());
                } else {
                    System.out.println(fileEntry.getName() + " nao e um ficheiro json!");
                }
            }
        }
        else{
            System.out.println("O nome do directorio " + folder.getName() + " nao existe!");
        }
        return filesList;
    }

    private static void getAllGraphs(){
        for(String file : filesList){
            String nameFile = file.substring(file.lastIndexOf('/') + 1, file.lastIndexOf('.'));

            ast = new AST(file);
            
            try{
            	analyzer = new Analyzer(ast);
    			analyzer.analyze();
    		} catch(Exception e){
    			Log.error("Error while analyzing the abstract syntax tree. Check json format.");
    			continue;
    		}
            
            /*output = new Output(analyzer);

            try {
                output.printControlGraph(String.format("export/%s_cfg.dot", nameFile));
                output.printDataGraph(String.format("export/%s_dfg.dot", nameFile));
                System.out.println("Foram criados os grafos para o ficheiro " + nameFile);
            } catch (IOException e) {
                System.out.println("Erro ao criar os grafos para o ficheiro " + nameFile);
            }*/
        }
    }

} 