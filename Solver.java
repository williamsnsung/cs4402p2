import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.LinkedList;

public class Solver {
    String algorithm;
    String varOrder;
    String valOrder;
    BinaryCSP csp;
    //TODO choose better data structure for domains
    ArrayList<PriorityQueue<Integer>> domains;
    ArrayList<Boolean> assigned;
    ArrayList<ArrayList<Integer>> arcs;
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
        this.assigned = new ArrayList<>();
        this.arcs = new ArrayList<>();
        for (int i = 0; i < csp.getNoVariables(); i++) {
            PriorityQueue<Integer> domain = new PriorityQueue<>();
            for (int j = csp.getLB(i); j < csp.getUB(i) + 1; j++) {
                domain.add(j);
            }
            this.domains.add(domain);
            this.assigned.add(false);
            this.arcs.add(new ArrayList<>());
        }
        for (BinaryConstraint constraint : csp.getConstraints()) {
            this.arcs.get(constraint.getFirstVar()).add(constraint.getSecondVar());
            this.arcs.get(constraint.getSecondVar()).add(constraint.getFirstVar());
        }
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public BinaryCSP getCsp() {
        return this.csp;
    }

    public boolean completeAssignment() {
        int assigned = 0;
        for (boolean bool : this.assigned) {
            if (bool) {
                assigned++;
            }
        }
        if (assigned == this.domains.size()) {
            return true;
        }
        return false;
    }

    public void printSolution() {
        System.out.println(this.searchNodes);
        System.out.println(this.arcRevisions);
        for (PriorityQueue<Integer> q : this.domains) {
            System.out.println(q.peek());
        }
        System.out.println();
    }

    public boolean revise(int futureVar, int var) {
        this.arcRevisions++;
        // if var is assigned and if var is not assigned
        for (BinaryConstraint constraint : this.csp.getConstraints()) {
            int firstVar = constraint.getFirstVar();
            int secondVar = constraint.getSecondVar();
            if (firstVar == futureVar && secondVar == var) {
                PriorityQueue<Integer> updatedFutureDomain = new PriorityQueue<>(this.domains.get(futureVar));
                for (Integer di : this.domains.get(futureVar)) {
                    boolean supported = false;
                    for (Integer dj : this.domains.get(var)) {
                        if(supported) {
                            break;
                        }
                        for (BinaryTuple t : constraint.getTuples()) {
                            if (t.matches(di, dj)) {
                                supported = true;
                            }
                        }
                    }
                    if (!supported) {
                        updatedFutureDomain.remove(di);
                    }
                }
                this.domains.set(futureVar, updatedFutureDomain);
            }
            else if (firstVar == var && secondVar == futureVar) {
                PriorityQueue<Integer> updatedFutureDomain = new PriorityQueue<>(this.domains.get(futureVar));
                for (Integer di : this.domains.get(futureVar)) {
                    boolean supported = false;
                    for (Integer dj : this.domains.get(var)) {
                        if(supported) {
                            break;
                        }
                        for (BinaryTuple t : constraint.getTuples()) {
                            if (t.matches(dj, di)) {
                                supported = true;
                            }
                        }
                    }
                    if (!supported) {
                        updatedFutureDomain.remove(di);
                    }
                }
                this.domains.set(futureVar, updatedFutureDomain);
            }
        }
        if (this.domains.get(futureVar).size() == 0) {
            return false;
        }
        return true;
    }

    public boolean reviseFutureArcs(LinkedList<Integer> varList, int var) {
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
    
    public ArrayList<PriorityQueue<Integer>> undoPruning() {
        ArrayList<PriorityQueue<Integer>> res = new ArrayList<>(this.domains);
        for (int i = 0; i < res.size(); i++) {
            res.set(i, new PriorityQueue<>(res.get(i)));
        }
        return res;
    }

    public PriorityQueue<Integer> assign(int var, int val) {
        PriorityQueue<Integer> original = new PriorityQueue<>(this.domains.get(var));
        this.domains.set(var, new PriorityQueue<>());
        this.domains.get(var).add(val);
        this.assigned.set(var, true);
        return original;
    }

    public void unassign(int var, PriorityQueue<Integer> original) {
        this.domains.set(var, original);
        this.assigned.set(var, false);
    }

    public void branchFCLeft(LinkedList<Integer> varList, int var, int val) {
        this.searchNodes++;
        PriorityQueue<Integer> original = assign(var, val);
        ArrayList<PriorityQueue<Integer>> undo = undoPruning();
        if (reviseFutureArcs(varList, var)) {
            LinkedList<Integer> copy = new LinkedList<>(varList);
            copy.removeFirstOccurrence(var);
            forwardChecking(copy);
        }
        this.domains = undo;
        unassign(var, original);
    }

    public void deleteValue(int var, int val) {
        this.domains.get(var).remove(val);
    }

    public void restoreValue(int var, int val) {
        this.domains.get(var).add(val);
    }

    

    public void branchFCRight(LinkedList<Integer> varList, int var, int val) {
        this.searchNodes++;
        deleteValue(var, val);
        ArrayList<PriorityQueue<Integer>> undo = undoPruning();
        if (this.domains.get(var).size() != 0) {
            if (reviseFutureArcs(varList, var)) {
                forwardChecking(varList);
            }
            this.domains = undo;
        }
        restoreValue(var, val);
    }

    public int selectVar(LinkedList<Integer> varList) {
        int selected = varList.peek();
        if (this.varOrder.equals("asc")) {
            selected = varList.iterator().next(); // retrieves the first element from the linked hash set
        }
        else if (this.varOrder.equals("sdf")){
            for (Integer i : varList) {
                if (this.domains.get(i).size() < this.domains.get(selected).size()) {
                    selected = i;
                }
            }
        }
        return selected;
    }

    public int selectVal(int var) {
        return this.domains.get(var).peek();
    }

    public void forwardChecking(LinkedList<Integer> varList) {
        if (completeAssignment()) {
            printSolution();
            return;
        }
        int var = selectVar(varList);
        int val = selectVal(var);
        branchFCLeft(varList, var, val);
        branchFCRight(varList, var, val);
    }

    public boolean ac3(int futureVar, int var) {
        this.arcRevisions++;
        boolean changed = false;
        // if var is assigned and if var is not assigned
        for (BinaryConstraint constraint : this.csp.getConstraints()) {
            int firstVar = constraint.getFirstVar();
            int secondVar = constraint.getSecondVar();
            if (firstVar == futureVar && secondVar == var) {
                PriorityQueue<Integer> updatedFutureDomain = new PriorityQueue<>(this.domains.get(futureVar));
                for (Integer di : this.domains.get(futureVar)) {
                    boolean supported = false;
                    for (Integer dj : this.domains.get(var)) {
                        if(supported) {
                            break;
                        }
                        for (BinaryTuple t : constraint.getTuples()) {
                            if (t.matches(di, dj)) {
                                supported = true;
                            }
                        }
                    }
                    if (!supported) {
                        updatedFutureDomain.remove(di);
                        changed = true;
                    }
                }
                this.domains.set(futureVar, updatedFutureDomain);
            }
            else if (firstVar == var && secondVar == futureVar) {
                PriorityQueue<Integer> updatedFutureDomain = new PriorityQueue<>(this.domains.get(futureVar));
                for (Integer di : this.domains.get(futureVar)) {
                    boolean supported = false;
                    for (Integer dj : this.domains.get(var)) {
                        if(supported) {
                            break;
                        }
                        for (BinaryTuple t : constraint.getTuples()) {
                            if (t.matches(dj, di)) {
                                supported = true;
                            }
                        }
                    }
                    if (!supported) {
                        updatedFutureDomain.remove(di);
                        changed = true;
                    }
                }
                this.domains.set(futureVar, updatedFutureDomain);
            }
        }
        return changed;
    }

    public boolean macAC3(int var) {
        LinkedList<BinaryTuple> q = new LinkedList<>();
        for (Integer i : this.arcs.get(var)) {
            q.add(new BinaryTuple(i, var));
        }
        while (q.size() > 0) {
            BinaryTuple cur = q.poll();
            if (ac3(cur.getVal1(), cur.getVal2())) {
                if (this.domains.get(cur.getVal1()).size() == 0) {
                    return false;
                }
                for (Integer i : this.arcs.get(cur.getVal1())) {
                    boolean dontAdd = false;
                    for (BinaryTuple binaryTuple : q) {
                        if ((binaryTuple.getVal1() == i || binaryTuple.getVal1() == cur.getVal2()) && binaryTuple.getVal2() == cur.getVal1()) {
                            dontAdd = true;
                        }
                    }
                    if (!dontAdd) {
                        q.add(new BinaryTuple(i, cur.getVal1()));
                    }
                }
            }
        }

        return true;
    }

    public void MAC3(LinkedList<Integer> varList) {
        this.searchNodes++;
        int var = selectVar(varList);
        int val = selectVal(var);
        PriorityQueue<Integer> original = assign(var, val);
        ArrayList<PriorityQueue<Integer>> undo = undoPruning();
        if (completeAssignment()) {
            printSolution();
            unassign(var, original);
            return;
        }
        else if (macAC3(var)) {
            LinkedList<Integer> copy = new LinkedList<>(varList);
            copy.removeFirstOccurrence(var);
            MAC3(copy);
        }
        this.domains = undo;
        unassign(var, original);
        deleteValue(var, val);
        if (this.domains.get(var).size() > 0) {
            undo = undoPruning();
            if (macAC3(var)) {
                MAC3(varList);
            }
            this.domains = undo;
        }
        restoreValue(var, val);
    }


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        if (args.length != 4) {
            System.out.println("Usage: java -jar P2.jar <*.csp> <fc|mac> <asc|sdf> <asc>") ;
            return ;
        }
        BinaryCSPReader reader = new BinaryCSPReader();
        Solver solver = new Solver(args[1], args[2], args[3], reader.readBinaryCSP(args[0]));
        LinkedList<Integer> varList = new LinkedList<>();
        for (int i = 0; i < solver.getCsp().getNoVariables(); i++) {
            varList.add(i);
        }
        if (solver.getAlgorithm().equals("fc")) {
            solver.forwardChecking(varList);
        }
        else if (solver.getAlgorithm().equals("mac")) {
            solver.MAC3(varList);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time in ms " + (endTime - startTime));
    }
}
