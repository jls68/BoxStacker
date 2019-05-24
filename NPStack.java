import java.util.LinkedList;
import java.util.Random;
import java.util.Collections;
import java.io.FileReader;
import java.awt.font.NumericShaper.Range;
import java.io.BufferedReader;

// 1314151 James Sheaf-Morrison

public class NPStack {
    // Declare list to hold all the boxes in the stack
    private static LinkedList<Box> stackedBoxes = new LinkedList<Box>();

    private static boolean canFit(int bottomIndex, Box x) {
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
        else if (canFit(bottomIndex, x)) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        // To help with testing fill in an empty args
        if (args.length == 0) {
            args = new String[1];
            args[0] = ("rand0010.boxes");
        }

        if (args.length > 0) {
            // Numebr of attempts per generation
            final int GENERATION = 4;

            // Declare list to hold all the available boxes in
            LinkedList<Box> readBoxes = new LinkedList<Box>();
            // Declare list to hold all the available boxes in
            LinkedList<Box> availBoxes = new LinkedList<Box>();
            // Declare list to hold all the boxes that can't fit onto stack
            LinkedList<Box> failedBoxes = new LinkedList<Box>();

            // Array of linked lists to hold each attempt
            LinkedList<LinkedList<Box>> gen;

            Random r = new Random();

            // Declare variables to use with stacking boxes
            int bottomIndex;
            int w, prevW = 0;
            int h, prevH = 0;
            int t;
            Box x;

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
                                Integer.parseInt(dimen[2])));
                    }
                }
                textFile.close();

                gen = new LinkedList<LinkedList<Box>>();
                // For each attempt in generation stack boxes with a different order of boxes in
                // the available list
                for (int g = 0; g < GENERATION; g++) {
                    // Add all boxes to available list
                    availBoxes.addAll(readBoxes);
                    // Grab random boxes from those available
                    for (int i = 0; i < readBoxes.size(); i++) {
                        // Take a random box from available
                        t = r.nextInt(availBoxes.size());
                        x = availBoxes.remove(t);
                        // Calculate which boxes can be add to stack
                        if (stackedBoxes.isEmpty()) {
                            stackedBoxes.add(x);
                        } else {
                            bottomIndex = stackedBoxes.size() - 1;
                            if (false == canFit(bottomIndex, x)) {
                                failedBoxes.add(x);
                            }
                        }
                    }

                    System.out.println("Attempt " + (g + 1));

                    System.out.println(
                            "Stacked boxes from top of stack at the top of the list to the bottom box at the end of list:");
                    // Display each box in stack
                    for (Box b : stackedBoxes) {
                        w = b.getBottomFace().Width();
                        h = b.getBottomFace().Height();
                        // Rotate boxes to fit if needed
                        if (w < prevW || h < prevH) {
                            t = w;
                            w = h;
                            h = t;
                        }
                        System.out.println(w + " " + h + " " + b.getCurrentHeight());
                        prevW = w;
                        prevH = h;
                    }

                    // Save attempt results
                    gen.add(stackedBoxes);
                    // Reset the stackedBoxes
                    stackedBoxes = new LinkedList<Box>();
                }

                System.out.println("This generation's attempts' total heights");
                for (LinkedList<Box> stack : gen) {
                    t = 0;
                    for (Box b : stack) {
                        t += b.getCurrentHeight();
                    }
                    System.out.println(t);
                }

                // Display any boxes we failed to stack
                /*
                 * if (!failedBoxes.isEmpty()) {
                 * System.out.println("These boxes did not make it onto the stack:"); // Display
                 * each box not in stack for (Box b : failedBoxes) {
                 * System.out.println(b.getFace(0).Width() + " " + b.getFace(0).Height() + " " +
                 * b.getHeight(0)); } }
                 */
            } catch (NumberFormatException e) {
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

        public Box(int width, int height, int depth) {
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
