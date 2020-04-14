package ie.lero.spare.forensics.assessment.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BigrapherStatesChecker {

  private String           bigraherType                     = JSONTerms.TRANSITIONS_BRS;
  private String           originalInputFolder;
  public static final int  PASS                             = 0;
  public static final int  TRANSITION_FILE_MISSING          = -1;
  public static final int  TRANSITION_FILE_PROBLEM          = -2;
  public static final int  STATES_MISSING                   = -3;
  public static final int  STATES_HAVE_PROBLEMS             = -4;
  public static final int  STATES_MISSING_AND_HAVE_PROBLEMS = -5;

  private Digraph<Integer> transitionsDigraph;
  private List<Integer>    statesNotFound;
  private List<Integer>    statesWithProblems;
  private static int  THRESHOLD        = 1000;
  
  // private String outputFolderName = "divided_states";

  public int checkStates(String folderName) {

    if (folderName == null || folderName.isEmpty()) {
      System.out.println("Folder name is not set");
      return 0;
    }

    originalInputFolder = folderName;

    // load transition file from folder (should be named transitions.json)
    // and convert it to digraph
    String transitionsFile = folderName + "/transitions.json";

    File file = new File(transitionsFile);

    if (!file.exists()) {
      return TRANSITION_FILE_MISSING;
    }

    // System.out.println("====== Create Transition digraph");
    transitionsDigraph = createDigraphFromJSON(transitionsFile);

    if (transitionsDigraph == null) {
      System.out.println("Something went wrong in Loading states transitions. Execution is terminated");
      return TRANSITION_FILE_PROBLEM;
    }

    statesNotFound = new LinkedList<Integer>();
    statesWithProblems = new LinkedList<Integer>();

    int numOfStates = transitionsDigraph.getNumberOfNodes();
    
    THRESHOLD = (int)(numOfStates*0.1);
    
    if(THRESHOLD < 100) {
      THRESHOLD = 100;
    }
    
    StateLoader stateLoader = new StateLoader(0, numOfStates);

    ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() / 2);

    pool.invoke(stateLoader);

    pool.shutdown();

    try {
      pool.awaitTermination(24, TimeUnit.HOURS);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();

    } finally {
      pool.shutdownNow();
    }

    // JSONObject jsonState;
    // JSONParser parser = new JSONParser();
    //
    // // check states
    // for (Integer state : transitionsDigraph.getNodes()) {
    // FileReader r;
    // try {
    // r = new FileReader(originalInputFolder + "/" + state + ".json");
    // jsonState = (JSONObject)parser.parse(r);
    //
    // jsonState = null;
    // } catch (FileNotFoundException ex) {
    // // TODO Auto-generated catch block
    // // ex.printStackTrace();
    // statesNotFound.add(state);
    // } catch (IOException | ParseException ex) {
    // // TODO Auto-generated catch block
    // // ex.printStackTrace();
    // statesWithProblems.add(state);
    // }
    //
    // }

    if (statesWithProblems.size() != 0 || statesNotFound.size() != 0) {
      if (statesWithProblems.size() > 0 && statesNotFound.size() == 0) {
        return STATES_HAVE_PROBLEMS;
      }

      if (statesWithProblems.size() == 0 && statesNotFound.size() > 0) {
        return STATES_MISSING;
      }

      if (statesWithProblems.size() > 0 && statesNotFound.size() > 0) {
        return STATES_MISSING_AND_HAVE_PROBLEMS;
      }
    }

    Runtime.getRuntime().gc();

    return PASS;
  }

  private Digraph<Integer> createDigraphFromJSON(String fileName) {

    Integer st1;
    Integer st2;
    double probability = -1;
    String label = null;
    Digraph<Integer> transitionGraph = new Digraph<Integer>();

    // transitionsFileLines = FileManipulator.readFileNewLine(fileName);

    JSONParser parser = new JSONParser();
    JSONObject obj;

    try {
      JSONArray ary;

      obj = (JSONObject)parser.parse(new FileReader(fileName));

      // if the transitions come from a brs file
      ary = (JSONArray)obj.get(JSONTerms.TRANSITIONS_BRS);

      // if the transitions come from pbrs
      if (ary == null) {
        ary = (JSONArray)obj.get(JSONTerms.TRANSITIONS__PROP_BRS);
        bigraherType = JSONTerms.TRANSITIONS__PROP_BRS;
      }

      if (ary == null) {
        ary = (JSONArray)obj.get(JSONTerms.TRANSITIONS__STOCHASTIC_BRS);
        bigraherType = JSONTerms.TRANSITIONS__STOCHASTIC_BRS;
      }

      // numberOfStates = new Integer(transitionsFileLines[0].split("
      // ")[0]);
      // //gets the number of states

      Iterator<JSONObject> iter = ary.iterator();
      JSONObject tmpObj = null;
      Object objGeneral = null;

      while (iter.hasNext()) {

        tmpObj = iter.next();

        // source state
        String srcState = tmpObj.get(JSONTerms.TRANSITIONS__SOURCE).toString();
        st1 = srcState != null
            ? Integer.valueOf(srcState)
            : -1;

        // destination state
        String desState = tmpObj.get(JSONTerms.TRANSITIONS__TARGET).toString();
        st2 = desState != null
            ? Integer.valueOf(desState)
            : -1;

        // probability. If there's no probability then its set to -1
        objGeneral = tmpObj.get(JSONTerms.TRANSITIONS__PROBABILITY);
        probability = objGeneral != null
            ? Double.parseDouble(objGeneral.toString())
            : -1;

        // label for action
        objGeneral = tmpObj.get(JSONTerms.TRANSITIONS__LABEL);
        label = objGeneral != null
            ? objGeneral.toString()
            : "";

        // if one of the states is not set to a proper state ( between 0
        // & Max-States-1)
        if (st1 == -1 || st2 == -1) {
          continue;
        }

        transitionGraph.add(st1, st2, probability, label);
      }

      // numberOfStates = transitionGraph.getNumberOfNodes();

    } catch (Exception ie) {
      ie.printStackTrace();
      return null;
    }

    return transitionGraph;
  }

  public Digraph<Integer> getTransitionsDigraph() {
    return this.transitionsDigraph;
  }

  public List<Integer> getStatesNotFound() {
    return this.statesNotFound;
  }

  public List<Integer> getStatesWithProblems() {
    return this.statesWithProblems;
  }

  public int getTransitionsNumber() {

    if (transitionsDigraph != null) {
      return transitionsDigraph.getNumberOfEdges();
    }

    return -1;
  }

  public int getStatesNumber() {

    if (transitionsDigraph != null) {
      return transitionsDigraph.getNumberOfNodes();
    }

    return -1;
  }

  public synchronized void updateStatesNotFound(int state) {

    statesNotFound.add(state);
  }

  public synchronized void updateStatesWithProblem(int state) {

    statesWithProblems.add(state);
  }

  class StateLoader extends RecursiveTask<Integer> {

    private static final long serialVersionUID = 1L;
    private int               indexStart;
    private int               indexEnd;
    // private LinkedList<Bigraph> states;
    // private Bigraph redex;
    // private HashMap<Integer, Bigraph> states;

    // for testing
    // protected int numOfParts = 0;

    public StateLoader(int indexStart, int indexEnd) {
      this.indexStart = indexStart;
      this.indexEnd = indexEnd;
      // states = new HashMap<Integer, Bigraph>();
    }

    @Override
    protected Integer compute() {
      // TODO Auto-generated method stub

      if ((indexEnd - indexStart) > THRESHOLD) {
        return ForkJoinTask.invokeAll(createSubTasks()).stream().map(new Function<StateLoader, Integer>() {

          @Override
          public Integer apply(StateLoader arg0) {
            // TODO Auto-generated method stub
            return 1;
          }

        }).reduce(1, new BinaryOperator<Integer>() {

          @Override
          public Integer apply(Integer arg0, Integer arg1) {
            // TODO Auto-generated method stub
            // arg0.putAll(arg1);
            return 1;
          }

        });

      } else {
        loadStates();

        return 1;
        // return states;
      }

    }

    private Collection<StateLoader> createSubTasks() {

      List<StateLoader> dividedTasks = new LinkedList<StateLoader>();

      int mid = (indexStart + indexEnd) / 2;

      dividedTasks.add(new StateLoader(indexStart, mid));
      dividedTasks.add(new StateLoader(mid, indexEnd));

      return dividedTasks;
    }

    private void loadStates() {

      JSONObject state;
      JSONParser parser = new JSONParser();

      for (int i = indexStart; i < indexEnd; i++) {
        try {
          // read state from file
          FileReader r = new FileReader(originalInputFolder + "/" + i + ".json");
          state = (JSONObject)parser.parse(r);
          r.close();
        } catch (FileNotFoundException ex) {

          // add state to states not found!
          updateStatesNotFound(i);

        } catch (IOException | ParseException ex) {

          // add state to states with problems
          updateStatesWithProblem(i);

        }
      }

      // clean
      state = null;
      parser = null;

      Runtime.getRuntime().gc();
    }
  }

}
