import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class Solver {
    String algorithm;
    String varOrder;
    String valOrder;
    BinaryCSP csp;
    HashMap <Integer, HashSet<Integer>> domains;
    int searchNodes;
    int arcRevisions;

    public Solver(String algorithm, String varOrder, String valOrder, BinaryCSP csp) {
        this.algorithm = algorithm;
        this.varOrder = varOrder;
        this.valOrder = valOrder;
        this.csp = csp;
        this.searchNodes = 0;
        this.arcRevisions = 0;
        for (int i = 0; i < csp.getNoVariables(); i++) {
            HashSet<Integer> domain = new HashSet<>();
            for (int j = csp.getLB(i); j < csp.getUB(i) + 1; j++) {
                domain.add(j);
            }
            this.domains.put(i, domain);
        }
    }

    public String getAlgorithm() {
        return this.algorithm;
    }



    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }



    public String getVarOrder() {
        return this.varOrder;
    }



    public void setVarOrder(String varOrder) {
        this.varOrder = varOrder;
    }



    public String getValOrder() {
        return this.valOrder;
    }



    public void setValOrder(String valOrder) {
        this.valOrder = valOrder;
    }



    public BinaryCSP getCsp() {
        return this.csp;
    }



    public void setCsp(BinaryCSP csp) {
        this.csp = csp;
    }

    public boolean completeAssignment(LinkedHashMap<Integer, Integer> varList) {
        int assigned = 0;
        for (Integer i : varList.values()) {
            if (i != -1) {
                assigned++;
            }
        }
        if (assigned == varList.size()) {
            return true;
        }
        return false;
    }

    public void printSolution(LinkedHashMap<Integer, Integer> varList) {
        System.out.println(this.searchNodes);
        System.out.println(this.arcRevisions);
        for (int i = 0; i < varList.size(); i++) {
            System.out.println(varList.get(i));
        }
        System.out.println();
    }

    //TODO
    public boolean revise(int futureVar, int var) {
        boolean revised = false;

        return revised;
    }

    public boolean reviseFutureArcs(LinkedHashMap<Integer, Integer> varList, int var) {
        boolean consistent = true;
        for (int futureVar : varList.keySet()) {
            if (futureVar != var && varList.get(futureVar) == -1) {
                consistent = revise(futureVar, var); //Prunes domain D(futureVar)
                if (!consistent) {
                    return false;
                }
            }
        }
        return true;
    }

    //TODO
    public void undoPruning() {

    }

    public void branchFCLeft(LinkedHashMap<Integer, Integer> varList, int var, int val) {
        varList.put(var, val);
        if (reviseFutureArcs(varList, var)) {
            varList.remove(var);
            forwardChecking(varList);
        }
        undoPruning();
        varList.put(var, -1);
    }

    public void branchFCRight(LinkedHashMap<Integer, Integer> varList, int var, int val) {
        varList.remove(var, val);
        if (this.domains.get(var).size() != 0) {
            if (reviseFutureArcs(varList, var)) {
                forwardChecking(varList);
            }
            undoPruning();
        }
        varList.put(var, val);
    }

    //TODO
    public int selectVar(LinkedHashMap<Integer, Integer> varList) {
        int selected = 0;

        return selected;
    }

    //TODO
    public int selectVal(int var) {
        int val = 0;

        return val;
    }

    public void forwardChecking(LinkedHashMap<Integer, Integer> varList) {
        if (completeAssignment(varList)) {
            printSolution(varList);
            return;
        }
        int var = selectVar(varList);
        int val = selectVal(var);
        branchFCLeft(varList, var, val);
        branchFCRight(varList, var, val);
    }


    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java -jar P2.jar <*.csp> <fc|mac> <asc|sdf> <asc>") ;
            return ;
        }
        BinaryCSPReader reader = new BinaryCSPReader();
        Solver solver = new Solver(args[1], args[2], args[3], reader.readBinaryCSP(args[0]));
        System.out.println(solver.getCsp());
    }
}
