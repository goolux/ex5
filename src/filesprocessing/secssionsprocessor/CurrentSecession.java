package filesprocessing.secssionsprocessor;

import filesprocessing.FileFacade;

import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * a singleton class which holds one section every time, and process the section to the proper name
 * of files.
 *
 * @author liorait, nlux.
 */
public class CurrentSecession {
    private static CurrentSecession instance = new CurrentSecession();

    /**
     * returns an instance of Currentecession
     * @return an instance of Currentecession
     */
    public static CurrentSecession getInstance() {
        return instance;
    }

    /* constants */
    private static final String VALUES_SEPARATOR = "#";
    private static final String REVERSE_SORT_KEY_VALUE = "REVERSE";
    private static final String NOT_OPERATION_KEY_WORD = "NOT";
    private static final String VALUE_SEPARATOR_KEY_WORD = "#";
    private static final String NOT_UPHOLD_OPERATOR = "NO";
    private static final String UPHOLD_OPERATOR = "YES";
    /*
     * the current path name.
     */
    private FileFacade path = null;
    /*
     * the current filter and sorter.
     */
    private FileFilter currentFileFilter;
    private Comparator<FileFacade> currentSort;

    /*
     * default constructor.
     */
    private CurrentSecession() {
        setDefaultValus();
    }

    /**
     * Sets the current path
     * @param path - the path to set
     */
    public void setCurrentPath(FileFacade path) {
        this.path = path;
    }

    /*
     * instance default values that are placed every new secession ittration.
     */
    private void setDefaultValus() {
        currentSort = SortFactory.getInstance().getAbsComparator();
        currentFileFilter = FilterFactory.getInstance().getAllFilter();
    }

    /**
     * set the current path name for extraction.
     *
     * @param path the path name.
     */
    public void setPath(FileFacade path) {
        this.path = path;
    }

    /**
     * set current section sorter
     * @param sorterKey the name of the sorter
     * @throws SecessionCreationException.SorterCreationException
     * cannot create sorter with the given order key name.
     */
    public void setSorter(String sorterKey) throws SecessionCreationException.SorterCreationException {
        Comparator<FileFacade> comparator = readSortKey(sorterKey);
        if (comparator == null) {
            currentSort = SortFactory.getInstance().getAbsComparator();
            throw new SecessionCreationException.SorterCreationException();
        } else {
            currentSort = comparator;
        }
    }

    /**
     * set current section filter.
     *
     * @param filterKey the name of the filter
     * @throws SecessionCreationException.FilterCreationException cannot create sorter with the fiven filter
     *                                                            key name.
     */
    public void setFilter(String filterKey) throws SecessionCreationException.FilterCreationException {
        FileFilter currentFileFilter = readFilterKey(filterKey);
        if (currentFileFilter == null) {
            this.currentFileFilter = FilterFactory.getInstance().getAllFilter();
            throw new SecessionCreationException.FilterCreationException();
        } else {
            this.currentFileFilter = currentFileFilter;
        }
    }

    /**
     * get current Session list of files.
     *
     * @return ordered array of files names.
     */
    public String[] getCurrentSessionOutput() {
        FileFacade[] files = path.listFiles(currentFileFilter);
        Arrays.sort(files, currentSort);
        String[] secessionFilesOutputNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            secessionFilesOutputNames[i] = files[i].getName();
        }
        setDefaultValus();
        return secessionFilesOutputNames;
    }

    /*
     * convert filter string into a FileFilter Object.
     * @param filterKey the filter requst string.
     * @return FileFilter
     */
    private FileFilter readFilterKey(String filterKey) {
        String[] values = filterKey.split(VALUE_SEPARATOR_KEY_WORD);
        String filterName = values[0];
        //indicative variables( if varible does not exists then it is null).
        Double firstDouble = null;
        Double secondDouble = null;
        Boolean filterParameterBoolean = null;
        String stringToFilter = null;
        int notOperationInclude = 0;
        boolean notOperation = false;
        for (int i = 1; i < values.length; i++) {
            if (values.length - 1 == i) {
                if (values[i].equals(NOT_OPERATION_KEY_WORD)) {
                    notOperation = true;
                    notOperationInclude++;
                    break;
                }
            }
            Double doubleValue = getDouble(values[i]);
            if (doubleValue != null && doubleValue >= 0) {//non negativ number
                if (firstDouble == null) {
                    stringToFilter = values[i];//if it aint ment to be a double.
                    firstDouble = doubleValue;
                } else if (secondDouble == null) {
                    secondDouble = doubleValue;
                }
            } else {
                Boolean booleanValue = isBoolean(values[i]);
                if (booleanValue != null) {
                    filterParameterBoolean = booleanValue;
                } else {
                    stringToFilter = values[i];
                }
            }
        }
        return getFilter(values.length - notOperationInclude, filterName, firstDouble,
                secondDouble, stringToFilter, filterParameterBoolean, notOperation);
    }

    /*
     * this method return a filter by the veriabls that had been read from the filter string
     * @param currentSize the number of valus without the not operation.
     * @param filterName the filter key name
     * @param firstDouble the double arg(if exists)
     * @param secondDouble the second doubler arg(if exists , for between filter)
     * @param stringToFilter the String search key (if exists)
     * @param firstBoolean the isUphold boolean(if exists)
     * @param notOperation is the Not operation exists
     * @return FileFilter if args are valid.
     */
    private FileFilter getFilter(int currentSize, String filterName, Double firstDouble, Double secondDouble,
                                 String stringToFilter, Boolean firstBoolean, boolean notOperation) {
        switch (currentSize) {
            case 1:
                return FilterFactory.getInstance().getFilter(filterName, notOperation);
            case 2:
                if (firstBoolean != null) {
                    return FilterFactory.getInstance().getFilter(filterName, firstBoolean, notOperation);
                } else if (firstDouble != null) {
                    FileFilter filterToSend = FilterFactory.getInstance().getFilter(filterName, firstDouble,
                            notOperation);
                    if (filterToSend == null) {//if the keyWord aint ment to be Double.
                        filterToSend = FilterFactory.getInstance().getFilter(filterName, stringToFilter,
                                notOperation);
                    }
                    return filterToSend;
                } else if (stringToFilter != null) {
                    return FilterFactory.getInstance().getFilter(filterName, stringToFilter, notOperation);
                }
                break;
            case 3:
                if (firstDouble != null && secondDouble != null) {
                    return FilterFactory.getInstance().getFilter(filterName, secondDouble, firstDouble,
                            notOperation);
                }
            default:
                break;
        }
        return null;
    }

    /*
     * return true if the string is boolean yes and false if it is no, return null if the string isnt yes/no
     */
    private Boolean isBoolean(String stringToCheck) {
        if (stringToCheck.equals(NOT_UPHOLD_OPERATOR)) {
            return false;
        } else if (stringToCheck.equals(UPHOLD_OPERATOR)) {
            return true;
        }
        return null;
    }

    /*
     * determine if the corrnt string is double.
     * @param stringToCheck
     * @return double value if the string is a double , null otherwise.
     */
    private Double getDouble(String stringToCheck) {
        try {
            return Double.parseDouble(stringToCheck);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Comparator<FileFacade> readSortKey(String sorterKey) {
        String[] values = sorterKey.split(VALUES_SEPARATOR);
        if (values.length > 2 || values.length < 1) {
            return null;
        }
        boolean isRevers = false;
        String sorterName = values[0];
        if (values.length == 2) {
            if (!values[1].equals(REVERSE_SORT_KEY_VALUE)) {
                return null;
            } else {
                isRevers = true;
            }
        }
        return SortFactory.getInstance().getComparator(sorterName, isRevers);
    }
}
