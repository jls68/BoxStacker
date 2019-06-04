import java.util.LinkedList;
import java.util.Random;
import java.util.Collections;
import java.io.FileReader;
import java.awt.font.NumericShaper.Range;
import java.io.BufferedReader;

class NPStack{

    private static boolean canFit(LinkedList<Box> stackedBoxes, int bottomIndex, Box x) {
        int boxHeight;
        Face toFit = stackedBoxes.get(bottomIndex).getBottomFace();
        LinkedList<Integer> fittingFaces = x.facesThatCanHold(toFit);
        // If stacked boxes can go on top of next box but that stack needs to be split
        if (0 < fittingFaces.size() && bottomIndex + 1 < stackedBoxes.size()) {
            // Remove any faces that can't fit on top of the bottem slice of the stack
            toFit = stackedBoxes.get(bottomIndex + 1).getBottomFace();
            fittingFaces = x.whichCanFitInside(toFit, fittingFaces);
        }
        // If there are faces that can fit then add the best one
        if (0 < fittingFaces.size()) {
            boxHeight = 0;
            // Find ideal orientation
            for (int orientation : fittingFaces) {
                // If this orientation offers a better height
                if (boxHeight < x.getHeight(orientation)) {
                    // Set boxHeight to current possible height
                    boxHeight = x.getHeight(orientation);
                    // Set orientation of the new box
                    x.setOrientation(orientation);
                }
            }
            stackedBoxes.add(bottomIndex + 1, x);
            return true;
        }
        // Look at next box up in stack
        bottomIndex--;
        // If there are no more boxes
        if (bottomIndex < 0) {
            // Check if the new box can go on top
            fittingFaces = x.whichCanFitInside(stackedBoxes.get(0).getBottomFace(), x.getAllFaceIndex());
            if (0 < fittingFaces.size()) {
                boxHeight = 0;
                for (int orientation : fittingFaces) {
                    // If this orientation offers a better height
                    if (boxHeight < x.getHeight(orientation)) {
                        // Set boxHeight to current possible height
                        boxHeight = x.getHeight(orientation);
                        // Set orientation of the new box
                        x.setOrientation(orientation);
                    }
                }
                stackedBoxes.addFirst(x);
                return true;
            }
        }
        // Check if box can be inserted higher into stack
        else if (canFit(stackedBoxes, bottomIndex, x)) {
            return true;
        }
        return false;
    }

    private static void printStack(LinkedList<Box> stackedBoxes, String breaker) {
        // Variables to use to rotate any blocks to fit
        int w, h;

        // Display each box in stack
        for (Box b : stackedBoxes) {
            w = b.getBottomFace().Width();
            h = b.getBottomFace().Height();
            // Rotate box so smallest of width or height is first
            if (w > h) {
                System.out.print(h + " " + w + " " + b.getCurrentHeight() + breaker);
            } else {
                System.out.print(w + " " + h + " " + b.getCurrentHeight() + breaker);
            }
        }
        if (breaker != "\n") {
            System.out.println();
        }
    }

    // Print stack method that also displays all boxes that could not be stacked
    private static void printStack(LinkedList<Box> stackedBoxes, LinkedList<Box> failedBoxes, String breaker) {
        printStack(stackedBoxes, breaker);
        // Display any boxes that failed to stack
        if (!failedBoxes.isEmpty()) {
            System.out.println("These boxes did not make it onto the stack:");
            // Display each box not in stack
            for (Box b : failedBoxes) {
                System.out.print(b.getFace(0).Width() + " " + b.getFace(0).Height() + " " + b.getHeight(0) + breaker);
            }
            if (breaker != "\n") {
                System.out.println();
            }
        }
    }

    public static void main(String[] args) {
        // To help with testing fill in an empty args
        if (args.length == 0) {
            args = new String[1];
            args[0] = ("/home/james/Documents/Compx301/COMPX301_A4/rand0020.boxes");
        }

        if (args.length > 0) {
            // Numebr of attempts per generation
            final int GENERATIONS = 8;
            // Numebr of extra generations to attempt with no improvment
            final int REPEATS = 3;

            // Declare list to hold all the available boxes in
            LinkedList<Box> readBoxes = new LinkedList<Box>();
            // Declare list to hold all the available boxes in
            LinkedList<Box> availBoxes = new LinkedList<Box>();
            // Declare list to hold all the boxes in the stack
            LinkedList<Box> stackedBoxes = new LinkedList<Box>();
            // Declare list to hold all the boxes that can't fit onto stack
            LinkedList<Box> failedBoxes = new LinkedList<Box>();

            // Array of two ints to hold each attempt; first is the generation, second is the order of the box in the stack, 
            // and the third determines if holding id, orientation of box, and the height of this box plus those above it
            int[][][] stacks;

            Random r = new Random();

            // Declare variables to use with stacking boxes
            // t is a temporary int value that is used in place of many local ints
            int bottomIndex, p1, p2, sum, bestStackSize = 1, t = 0, repeats = 0;;
            int toRecall = GENERATIONS + 1;
            Box x, x2;
            boolean roomForImprovement = true;
            // Hold the height of each stack to get the chance of it being a parent
            int[] parentChance = new int[GENERATIONS];

            // Declare variables to use while reading from file
            String textLine = "(No line read)";
            String[] dimen;
            try {
                BufferedReader textFile = new BufferedReader(new FileReader(args[0]));
                // Read in the boxes from file
                while (null != (textLine = textFile.readLine())) {
                    dimen = textLine.split(" ");
                    if (dimen.length > 3) {
                        System.out.println(
                                "Less than 3 dimensions given for a box in file" + args[0] + ". Line was " + textLine);
                    } else {
                        readBoxes.add(new Box(Integer.parseInt(dimen[0]), Integer.parseInt(dimen[1]),
                                Integer.parseInt(dimen[2]), t));
                    }
                    // t increments to give a new id for the next box read
                    t++;
                }
                textFile.close();

                // The toRecall value refers to the amount of attempts per generation plus the best overall stack
                // The 3 refers to the three integers to remember for each box; id, orientation, and local height
                stacks = new int[toRecall][readBoxes.size()][3];

                System.out.println(
                        "Stacked boxes are displayed from top of stack at the start of the list to the bottom box at the end of list.");

                while (roomForImprovement) {
                    // Stop after this generation unless stated otherwise
                    roomForImprovement = false;

                    System.out.println("New Generation of attempts:\n");
                    for (int g = 0; g < GENERATIONS; g++) {
                        // Add all boxes to available list
                        availBoxes.addAll(readBoxes);

                        // For each attempt in generation stack boxes with a different order of boxes in
                        // the available list
                        
                        // If parentChance is not 0 and is not the last in generation then merge boxes into next generation
                        if (parentChance[g] != 0 && g != GENERATIONS-1) {
                            // Get total sum
                            sum = 0;
                            for (int s : parentChance) {
                                sum += s;
                            }
                            // Pick first parent
                            p1 = r.nextInt(sum);
                            for (int i = 0; i < GENERATIONS; i++) {
                                // If not this town
                                if (p1 > parentChance[i]) {
                                    // Reduce p1
                                    p1 -= parentChance[i];
                                }
                                // Else this town is the parent
                                else
                                {
                                    p1 = i;
                                    break;
                                }
                            }

                            p2 = r.nextInt(sum - parentChance[p1]);

                            for (int i = 0; i < GENERATIONS; i++) {
                                // If same as first parent then skip
                                if (i != p1)
                                {
                                    // If not this satck
                                    if (p2 > parentChance[i])
                                    {
                                        // Reduce p2
                                        p2 -= parentChance[i];
                                    }
                                    // Else this stack is the parent
                                    else
                                    {
                                        p2 = i;
                                        break;
                                    }
                                }
                            }
                            System.out.println("Parents are " + p1 + " & " + p2);

                            for (int i = 0; i < readBoxes.size(); i++) {
                                if (stacks[p1][i][0] == 0 && stacks[p2][i][0] == 0){
                                    break;
                                }
                                // Get box from first parent
                                x = readBoxes.get(stacks[p1][i][0]);
                                // Get box from second parent
                                x2 = readBoxes.get(stacks[p2][i][0]);
                                // Merge the two parents
                                for (Box b : availBoxes) {
                                    if(b == x){
                                        availBoxes.remove(b);
                                        // Calculate if the box can be add to stack
                                        if (stackedBoxes.isEmpty()) {
                                            stackedBoxes.add(b);
                                        } else {
                                            bottomIndex = stackedBoxes.size() - 1;
                                            if (false == canFit(stackedBoxes, bottomIndex, b)) {
                                                failedBoxes.add(b);
                                            }
                                        }
                                        break;
                                    }
                                }
                                // No need to try to add the same block
                                if (x != x2){
                                    for (Box b : availBoxes) {
                                        if(b == x2){
                                            availBoxes.remove(b);
                                            // Calculate if the box can be add to stack
                                            if (stackedBoxes.isEmpty()) {
                                                stackedBoxes.add(b);
                                            } else {
                                                bottomIndex = stackedBoxes.size() - 1;
                                                if (false == canFit(stackedBoxes, bottomIndex, b)) {
                                                    failedBoxes.add(b);
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        
                        t = availBoxes.size();
                        // Grab random boxes from those available
                        for (int i = 0; i < t; i++) {
                            // Take a random box from available
                            t = r.nextInt(availBoxes.size());
                            x = availBoxes.remove(t);
                            // Calculate which boxes can be add to stack
                            if (stackedBoxes.isEmpty()) {
                                stackedBoxes.add(x);
                            } else {
                                bottomIndex = stackedBoxes.size() - 1;
                                if (false == canFit(stackedBoxes, bottomIndex, x)) {
                                    failedBoxes.add(x);
                                }
                            }
                        }

                        System.out.print("Attempt " + (g + 1) +": ");

                        printStack(stackedBoxes, ", ");
                        
                        System.out.print("This attempt's total height is ");
                        sum = 0;
                        for (int b = 0; b < stackedBoxes.size(); b++) {
                            x =  stackedBoxes.get(b);
                            sum += x.getCurrentHeight();
                            // Save information
                            stacks[g][b][0] = x.getID();
                            stacks[g][b][1] = x.getOri();
                            stacks[g][b][2] = sum;
                        }
                        parentChance[g] = sum;
                        System.out.print(sum + "\n\n");

                        // We will use the current size of the stack often so save it in a temp variable for easy access
                        t = stackedBoxes.size();

                        if (stacks[g][t-1][2] != sum){
                            System.out.println("Error with setting the sum height of current stack");
                        }


                        // If this is the tallest stack save it
                        if (stacks[GENERATIONS][bestStackSize-1][2] < sum){
                            for(int i = 0; i < bestStackSize || i < t; i++){
                                stacks[GENERATIONS][i][0] = stacks[g][i][0];
                                stacks[GENERATIONS][i][1] = stacks[g][i][1];
                                stacks[GENERATIONS][i][2] = stacks[g][i][2];
                            }
                            // Set new size of best stack
                            bestStackSize = t;
                            // There has been improvement so keep going
                            roomForImprovement = true;
                            repeats = 0;
                        }
                        else{
                            repeats++;
                            if(repeats < REPEATS){
                                roomForImprovement = true;
                            }
                        }
                        // Reset the stackedBoxes and availableBoxes
                        stackedBoxes.clear();
                        availBoxes.clear();
                    }
                } // End while for repeating creating generations

                System.out.println("Best stack at height " + stacks[GENERATIONS][bestStackSize-1][2] + " is:");
                for(int i = 0; i < bestStackSize; i++){
                    x = readBoxes.get(stacks[GENERATIONS][i][0]);
                    x.setOrientation(stacks[GENERATIONS][i][1]);
                    stackedBoxes.add(x);
                }
                printStack(stackedBoxes, "\n");

            } catch (NumberFormatException ex) {
                System.out.println("A dimension for the box on the line : " + textLine
                        + " was unable to be parsed into an integer");
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        } else {
            System.out.println(
                    "Input format incorrect. Requires filename of file containing the deminsions of boxes, that is three intergers seperated by a space on one line per box, as command-line argument");
        }
    }

    private static class Box {
        // Using a list of points as it offers an X and a Y value which is all we need
        private Face[] uniqueFaces;
        private int orientation = 0;
        private int id;

        public Box(int width, int height, int depth, int id) {
            this.id = id;
            // Convert the box's dimensions into a series of uniqueFaces

            // If box is a cube
            if (width == height && width == depth) {
                // Then add only one face to uniqueFaces
                uniqueFaces = new Face[1];
                uniqueFaces[0] = (new Face(depth, height));
            }
            // else find if a face is square
            else if (width == height || height == depth) {
                // Then add two unique faces to uniqueFaces
                uniqueFaces = new Face[2];
                // Have the first dimension of the next box be the unused dimension
                uniqueFaces[0] = (new Face(width, height)); // The square face || rectangle face
                uniqueFaces[1] = (new Face(depth, height)); // The rectangle face || The square face
            } else if (width == depth) {
                // Then add two unique faces to uniqueFaces
                uniqueFaces = new Face[2];
                // Have the first dimension of the next box be the unused dimension
                uniqueFaces[0] = (new Face(width, depth)); // The square face
                uniqueFaces[1] = (new Face(height, depth)); // The rectangle face
            }
            // As this case produces the same output as width == height I just added the or
            // to the first else if case
            /*
             * else if(height == depth){ // Then add two unique faces to uniqueFaces
             * uniqueFaces = new Face[2]; uniqueFaces[0] = (new Face(width, height)); // The
             * rectangle face uniqueFaces[1] = (new Face(depth, height)); // The square face
             * }
             */
            // Else width, height, and depth are different
            else {
                // Then add three unique faces to uniqueFaces
                uniqueFaces = new Face[3];
                // Have the first dimension of the next box be the unused dimension
                uniqueFaces[0] = (new Face(height, width));
                uniqueFaces[1] = (new Face(depth, height));
                uniqueFaces[2] = (new Face(width, depth));
            }
        }

        public int getID() {
            return id;
        }

        public int getOri() {
            return orientation;
        }

        public Face getBottomFace() {
            return getFace(orientation);
        }

        public void setOrientation(int bottomFaceIndex) {
            orientation = bottomFaceIndex;
        }

        public Face getFace(int index) {
            // If specified face is within index
            if (index < uniqueFaces.length && index >= 0) {
                return uniqueFaces[index];
            } else {
                System.out.println("getFace method given index of " + index + ", which was out of range");
                // By default return first face
                return uniqueFaces[0];
            }
        }

        public LinkedList<Integer> getAllFaceIndex() {
            LinkedList<Integer> faces = new LinkedList<Integer>();
            // If specified face is within index
            for (int i = 0; i < uniqueFaces.length; i++) {
                faces.add(i);
            }
            return faces;
        }

        public int getHeight(int bottomFaceIndex) {
            // If specified face is within index
            if (bottomFaceIndex < uniqueFaces.length && bottomFaceIndex >= 0) {
                int index;
                // If bottomFaceIndex points to end on array
                if (bottomFaceIndex + 1 == uniqueFaces.length) {
                    // Set index to 0
                    index = 0;
                }
                // Else find index of next face
                else {
                    index = bottomFaceIndex + 1;
                }
                return uniqueFaces[index].Height();
            } else {
                System.out.println("getHeight method given index of " + bottomFaceIndex + ", which was out of range");
                // By default return first face's height
                return uniqueFaces[0].Height();
            }
        }

        public int getCurrentHeight() {
            return getHeight(orientation);
        }

        // Return all faces that the face to compare can fit inside
        public LinkedList<Integer> facesThatCanHold(Face compare) {
            LinkedList<Integer> fittingFaces = new LinkedList<Integer>();
            // Check each unique face
            for (int i = 0; i < uniqueFaces.length; i++) {
                // If the width and height of the compare face fits
                if (compare.fitsInto(uniqueFaces[i])) {
                    fittingFaces.add(i);
                }
            }
            return fittingFaces;
        }

        // Return all faces that the list of faces provided can fit the face to compare
        public LinkedList<Integer> whichCanFitInside(Face compare, LinkedList<Integer> faces) {
            LinkedList<Integer> output = new LinkedList<Integer>();
            // Check each unique face
            for (int i : faces) {
                // If the width and height of the compare face fits
                if (uniqueFaces[i].fitsInto(compare)) {
                    output.add(i);
                }
            }
            return output;
        }
    }

    // A class to hold the two dimensions of a face of a box
    private static class Face {
        private int height;
        private int width;

        public Face(int height, int width) {
            this.height = height;
            this.width = width;
        }

        public int Height() {
            return height;
        }

        public int Width() {
            return width;
        }

        public boolean fitsInto(Face compare) {
            // Check if Face fits inside compare in either orientation
            return (height < compare.Height() && width < compare.Width())
                    || (width < compare.Height() && height < compare.Width());

            /*
             * // Check if Face fits inside compare and change then check orientation if not
             * if (height < compare.Height() && width < compare.Width()){ return true; }
             * else if (width < compare.Height() && height < compare.Width()){ int temp =
             * width; width = height; height = temp; return true; } return false;
             */
        }
    }
}