import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class binarytreereg {
    int score;
    //return the final score to the user
    int getscore () {
        return score;
    }
    //check if the nodes that have been travered match all descriptions specified 
    boolean validate (String generatedpath, String[] requirements) {
        boolean alrightsofar = true;
        int index = 0; 
        while(alrightsofar && index < requirements.length){ //while the requirements are still being met
            alrightsofar = generatedpath.contains(requirements[index]); //ensure the requirements are being met
            index++;
        }
        return alrightsofar;
    }
    //traverses all the nodes in order to find a solution
    int travel (nodecontainer root,  String path) {

        int score = 0;
        node parentnode = root.root;
        ArrayList<Integer> donenodes = new ArrayList<>(); 
        String[] splitpath = path.split(",");
        String pathtaken = parentnode.description+",";

        while( parentnode!=null && score == 0 ){ //while we haven't found the score or travered all of the tree already
            node left = parentnode.left;
            node right = parentnode.right;
            if(left!=null && !isdone(donenodes, left.index)){ //check the left node
                donenodes.add(left.index); //specify that this route isn't taken again 
                left.parent = parentnode; //set the parent                 
                parentnode = left; //make this node the new parent node
                pathtaken+=parentnode.description+","; //keep track of the route taken from the start node
            } else if(right!=null && !isdone(donenodes, right.index)){ 
                donenodes.add(right.index);
                right.parent = parentnode;
                parentnode = right;
                pathtaken+=parentnode.description+",";
            }else {
                if(parentnode.score!=0 && validate(pathtaken, splitpath)){ 
                    //if you have reached a dead end or a leaf check to see if route meets requirements 
                    score = parentnode.score; 
                }
                String parttoremove = parentnode.description+","; 
                //since this is a dead end, and you need to go back up the tree
                //remove the attribute of the node  
                pathtaken = pathtaken.substring(0, (pathtaken.length())-parttoremove.length());  
                //do this by taking the substring  
                //set the parent node 
                parentnode = parentnode.parent;
            }



        }
        return score;
    }
    //constructor running the function
    binarytreereg (List<String> schema, List<String> questions) {
        nodecontainer root = new nodecontainer();
        List<String> data = sanitise(schema);
        root = createbinarytree(root, data);
        int result = 0;
        
        for(String ques : questions){
            // System.out.println(ques);
            result += travel(root, ques); 
        }
        
        score = result;
    }
    //santises the data tree so that node indexes occur at regular intervals 
    List<String> sanitise (List<String> change) {

        HashMap<String, String> mapValues = new HashMap<>();

        //calibrate the index of each node
        for (int k = 0; k < change.size(); k++) {
            String current = change.get(k);
            String[] parts = current.split(",");
            String solution = Integer.toString(k);
            mapValues.put(parts[0], solution);
            parts[0] = solution;

            current = "";
            int b = 0;
            for(String item : parts){
                current+= item + (b==(parts.length-1) ? "" : ",");
                b++;
            }          
            change.set(k, current);
        }


        //calibrate the child nodes to match the new indexes
        for (int k = change.size()-1; k >= 0; k--) {
            String current = change.get(k);
            String[] parts = current.split(",");
            // String solution = Integer.toString(k);
            // mapValues.put(parts[0], solution);
            if(!parts[2].equals("-1")){
                parts[2] = mapValues.get(parts[2]);
            }
            if(!parts[3].equals("-1")){
                parts[3] = mapValues.get(parts[3]);
            }            
            current = "";
            int b = 0;
            for(String item : parts){
                current+= item + (b==(parts.length-1) ? "" : ",");
                b++;
            }          
            change.set(k, current);
        }



        return change;
    }
    //generates a binary tree
    nodecontainer createbinarytree (nodecontainer root, List<String> schema) {

        String[] schema0split = schema.get(0).split(",");
        //split the first node
        int first = Integer.parseInt(schema0split[0]);
        schema.set(0, (schema.get(0) + ",0"));
        //THIS TELLS US THE PARENT OF FIRST NODE TO BEING 0 
        ArrayList<Integer> donenodes = new ArrayList<>(); 
        //defines which nodes have been traversed so that the same route is travered multiple times
        int parentnodeindex = 0;
        
        int centred = 0;
        int max = 0;

        //specifies the maxium number of traversals needed to generate all nodes
        if(!schema0split[2].equals("-1")){
            max++;
        }
        if(!schema0split[3].equals("-1")){
            max++;
        }        

        //add first node so we have a reference point to start from
        root.addFirstNode(schema0split);

        while (centred<max){ //whilst we haven't traversed all of the tree
            String[] parentnodedetails = schema.get(parentnodeindex).split(",");
            int leftindex = Integer.parseInt(parentnodedetails[2]);  
            int rightindex = Integer.parseInt(parentnodedetails[3]);  
            //check to see if the parent node has a left/right index and ensure that if it does 
            //it has not already been traversed
            if(leftindex!=-1 && !isdone(donenodes, leftindex)){
                schema.set(leftindex, (schema.get(leftindex) + ","+parentnodeindex) );
                //update the schema that was originally defined so that we know who its parent is 
                parentnodeindex = leftindex;
                //make this the new parent index
                donenodes.add(leftindex);
                //finally append the node to the parent node
                root.appendnode(parentnodedetails, schema.get(leftindex).split(","), true);
            }else if(rightindex!=-1 && !isdone(donenodes, rightindex)){
                schema.set(rightindex, (schema.get(rightindex) + ","+parentnodeindex) );
                parentnodeindex = rightindex;
                donenodes.add(rightindex);
                root.appendnode(parentnodedetails, schema.get(rightindex).split(","), false);
            }else {
                //you have reached a dead end so you must begin to approach the parent node 
                parentnodeindex = Integer.parseInt(parentnodedetails[5]);
                centred = parentnodeindex==first ? centred + 1 : centred;
            }
        }

        return root;

    }
    //checks to see whether indexes are in a list or not
    boolean isdone (ArrayList<Integer> list, int index) { 
        boolean found = false;
        for(int a = 0; a < list.size(); a++){
            if(list.get(a)==index){
                found = true;
                break;
            }
        }
        return found;
    } 
}
//this is a adjacency list to give details of nodes
class mapper  {
        int index;
        String location;
        String attrb;
        boolean haschildleaf;
        int leafscore;
        boolean haschildnode;

        public mapper(int index, String location, String attrb) {
            this.index = index;
            this.location = location;
            this.attrb = attrb;
        }  
        
        void addchildleaf (int score) {
            if (score!=0) {
                this.haschildleaf = true;
                this.leafscore = score;
            }
        }

        void haschildnode () {
            haschildnode = true;
        }
        
        void printalldetails () {
            System.out.println(index + ") location is:" + location + " a" + attrb + " child leaf " + haschildleaf + " leaf score " + leafscore + " has child node "+ haschildnode);
        }
    } 
//manages production of nodes
class nodecontainer {
        node root;
        ArrayList<mapper> map;

        public nodecontainer () {
            root = null;
            map = new ArrayList<>();
        } 

        void appendnode (String[] parent, String[] child, boolean left) {

            //define what a child node is
            node childnode = new node();
            childnode.index = Integer.parseInt(child[0]);
            childnode.description = child[1];
            childnode.score = Integer.parseInt(child[4]);
            childnode.tail = true;
            //find the parentmetadata so that we know where to append the child node to
            mapper parentmetadata = getmapper(Integer.parseInt(parent[0])); 
            String[] parentroute = parentmetadata.location.split(",");
            int index = 0;
            node transformer = root;

            
            while(index < parentroute.length){
                //the first node will always have a parentroute of [] so skip the first node and focus 
                //on those that have a specified route   
                if(parentroute[index].equals("left") || parentroute[index].equals("right")){
                    //figure out what node is next 
                    node nextnode = parentroute[index].equals("left") ? transformer.left : transformer.right;
                    //store the parent
                    node tempparent = transformer;
                    transformer = nextnode;
                    //make note of who the parent is so you can get back
                    transformer.parent = tempparent;
                }
                index++;
            }


            //append the childnode to the parent  
            if(left){
                transformer.left = childnode;
            }
            else {
                transformer.right = childnode;
            }

            //store this transformer and all its other contents
            while(transformer.parent!=null){
                transformer = transformer.parent;
            }

            root = transformer;

            //define a route for accessing the childnode
            String childroute = parentmetadata.location + (left ? "left," : "right,");
            mapper childmapper = new mapper(childnode.index, childroute, child[1]);
            map.add(childmapper);
        }



        void addFirstNode (String[] parent) {
                //define parent node
                node parentroot = new node();
                parentroot.index = Integer.parseInt(parent[0]);
                parentroot.description = parent[1];
                parentroot.score = Integer.parseInt(parent[4]);
                parentroot.parent = null;
                root = parentroot;
                mapper temp = new mapper(0, "", parent[1]); 
                map.add(temp);
        }

        //get detials for a particular node
        mapper getmapper (int wanted) {
            mapper output = null;

            for(int b = 0; b < map.size(); b++){
                if(map.get(b).index==wanted){
                    output = map.get(b);
                    break;
                }
            }

            return output;

        }

    }
//Definition of a node.
class node {
        int index;
        node parent;
        node right;
        node left;
        String description;
        int score;
        boolean tail = false;
}
