import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.LinkedHashSet;

public class Solver {
    String algorithm;
    String varOrder;
    String valOrder;
    BinaryCSP csp;
    ArrayList<PriorityQueue<Integer>> domains;
    ArrayList<Integer> assignments; 
    int searchNodes;
    int arcRevisions;

    public Solver(String algorithm, String varOrder, String valOrder, BinaryCSP csp) {
        this.algorithm = algorithm;
        this.varOrder = varOrder;
        this.valOrder = valOrder;
        this.csp = csp;
        this.searchNodes = 0;
        this.arcRevisions = 0;
        this.domains = new ArrayList<>();
        this.assignments = new ArrayList<>();
        for (int i = 0; i < csp.getNoVariables(); i++) {
            PriorityQueue<Integer> domain = new PriorityQueue<>();
            for (int j = csp.getLB(i); j < csp.getUB(i) + 1; j++) {
                domain.add(j);
            }
            this.domains.add(domain);
            this.assignments.add(-1);
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

    public boolean completeAssignment() {
        int assigned = 0;
        for (Integer i : this.assignments) {
            if (i != -1) {
                assigned++;
            }
        }
        if (assigned == this.assignments.size()) {
            return true;
        }
        return false;
    }

    public void printSolution() {
        System.out.println(this.searchNodes);
        System.out.println(this.arcRevisions);
        for (Integer i : this.assignments) {
            System.out.println(i);
        }
        System.out.println();
    }

    //TODO
    public boolean revise(int futureVar, int var) {
        boolean revised = false;

        return revised;
    }

    public boolean reviseFutureArcs(LinkedHashSet<Integer> varList, int var) {
        boolean consistent = true;
        for (int futureVar : varList) {
            if (futureVar != var) {
                consistent = revise(futureVar, var); //Prunes domain D(futureVar)
                if (!consistent) {
                    return false;
                }
            }
        }
        return true;
    }

    public void branchFCLeft(LinkedHashSet<Integer> varList, int var, int val) {
        this.assignments.set(var, val);
        ArrayList<PriorityQueue<Integer>> undoPruning = new ArrayList<>(this.domains);
        if (reviseFutureArcs(varList, var)) {
            LinkedHashSet<Integer> copy = new LinkedHashSet<>(varList);
            copy.remove(var);
            forwardChecking(copy);
        }
        this.domains = undoPruning;
        this.assignments.set(var, -1);
    }

    public void deleteValue(int var, int val) {
        this.domains.get(var).remove(val);
    }

    public void restoreValue(int var, int val) {
        this.domains.get(var).add(val);
    }

    public void branchFCRight(LinkedHashSet<Integer> varList, int var, int val) {
        deleteValue(var, val);
        ArrayList<PriorityQueue<Integer>> undoPruning = new ArrayList<>(this.domains);
        if (this.domains.get(var).size() != 0) {
            if (reviseFutureArcs(varList, var)) {
                forwardChecking(varList);
            }
            this.domains = undoPruning;
        }
        restoreValue(var, val);
    }

    public int selectVar(LinkedHashSet<Integer> varList) {
        int selected = 2147483647; // setting selected to the maximum value for an int, used for finding the smallest domain
        if (this.varOrder.equals("asc")) {
            selected = varList.iterator().next(); // retrieves the first element from the linked hash set
        }
        else if (this.varOrder.equals("sdf")){
            for (Integer i : varList) {
                if (this.domains.get(i).size() < selected) {
                    selected = this.domains.get(i).size();
                }
            }
        }
        return selected;
    }

    public int selectVal(int var) {
        return this.domains.get(var).peek();
    }

    public void forwardChecking(LinkedHashSet<Integer> varList) {
        if (completeAssignment()) {
            printSolution();
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
