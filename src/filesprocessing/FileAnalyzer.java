package filesprocessing;


import filesprocessing.secssionsprocessor.CurrentSecession;
import filesprocessing.secssionsprocessor.SecessionCreationException;

import java.util.ArrayList;
import java.lang.*;

/*
 *
 */
class FileAnalyzer {
    private static FileAnalyzer instance = new FileAnalyzer();
    /*--constants--*/
    private static final String FILTER_HEADLINE = "FILTER";
    private static final String ORDER_HEADLINE = "ORDER";
    private static final String TYPE_I_ERROR_MSG_STR_FORMAT = "Warning in line %d\n";
    private static final String DEFAULT_SORTER = "abs";

    /*
     * singlton constractor.
     */
    private FileAnalyzer() {
    }

    /*
     * @return file anlayzer instance.
     */
    static FileAnalyzer getInstance() {
        return instance;
    }

    /* This method goes through the array list that contains the file data, First,
     * it looks for type 2 exceptions, if found,
     * it throws exceptions. If there are no type 2 exceptions, it goes over the file, and for each section
     *it filters and orders the files
     */
    void analyzeStringList(ArrayList<String> fileData) throws TypeTwoExceptions.BadFilterSectionName,
            TypeTwoExceptions.BadOrderSectionName, TypeTwoExceptions.BadFormatFile {
        checkTypeTwoErrors(fileData); // Checks if there are type 2 errors in the file
        String filterValue;
        String orderValue;
        int filterLine;
        int orderLine = 0;
        // This loop goes over the array list and filters and sorters files by each section
        for (int i = 0; i < fileData.size(); i++) {
            i++;
            if (i >= fileData.size()) {
                break;
            }
            filterValue = fileData.get(i);
            filterLine = ++i;//pass to order headLine
            i++;//pass the order headline
            if (fileData.size() <= i || fileData.get(i).equals(FILTER_HEADLINE)) {
                i--;//if the headline is at the currnt pointer i.
                orderValue = DEFAULT_SORTER;
            } else {
                orderValue = fileData.get(i);
                orderLine = i+1;
            }
            try {
                CurrentSecession.getInstance().setFilter(filterValue);
            } catch (SecessionCreationException.FilterCreationException e) {
                System.err.printf(TYPE_I_ERROR_MSG_STR_FORMAT, filterLine);
            }
            try {
                CurrentSecession.getInstance().setSorter(orderValue);
            } catch (SecessionCreationException.SorterCreationException e) {
                System.err.printf(TYPE_I_ERROR_MSG_STR_FORMAT, orderLine);
            }
            String[] outPutData = CurrentSecession.getInstance().getCurrentSessionOutput();
            printFiles(outPutData);
        }
    }

    private void checkTypeTwoErrors(ArrayList<String> fileData) throws
            TypeTwoExceptions.BadFilterSectionName,
            TypeTwoExceptions.BadOrderSectionName, TypeTwoExceptions.BadFormatFile {
        // This loop iterates through the array, for each section checks if in the section exists type
        // 2 errors
        for (int i = 0; i < fileData.size(); i++) {
            i = checkFilter(fileData, i);
            i = checkOrder(fileData, i);
        }
    } // end of checkTypeTwoErrors method


    private int checkFilter(ArrayList<String> fileData, int lineNumber)
            throws TypeTwoExceptions.BadFilterSectionName, TypeTwoExceptions.BadFormatFile {
        if (fileData.get(lineNumber).equals(FILTER_HEADLINE)) {
            lineNumber++;
            if (fileData.size() > lineNumber) {
                return ++lineNumber;
            }
        }
        if (fileData.size()<=lineNumber){
            throw new TypeTwoExceptions.BadFormatFile();
        }
        if (fileData.get(lineNumber).toUpperCase().equals(FILTER_HEADLINE)) {
            throw new TypeTwoExceptions.BadFilterSectionName();
        } else {
            throw new TypeTwoExceptions.BadFormatFile();
        }
    } // end of checkFilter method

    private int checkOrder(ArrayList<String> fileData, int lineNumber)
            throws TypeTwoExceptions.BadOrderSectionName, TypeTwoExceptions.BadFormatFile {
        if (fileData.size() > lineNumber) {
            if (fileData.get(lineNumber).equals(ORDER_HEADLINE)) {
                if (fileData.size() > lineNumber + 1) {
                    if (fileData.get(lineNumber + 1).equals(FILTER_HEADLINE)) {
                        return lineNumber;
                    }
                }
                return ++lineNumber;
            }
        }
        if (fileData.size()<=lineNumber){
            throw new TypeTwoExceptions.BadFormatFile();
        }
        if (fileData.get(lineNumber).toUpperCase().equals(ORDER_HEADLINE)) {
            throw new TypeTwoExceptions.BadOrderSectionName();
        } else {
            throw new TypeTwoExceptions.BadFormatFile();
        }
    }

    // private method to print the names of the filtered and sorted files
    private void printFiles(String[] filesToPrint) {
        for (int i = 0; i < filesToPrint.length; i++) {
            System.out.println(filesToPrint[i]);
        }
    }
}
