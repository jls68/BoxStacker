import java.util.List;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.Console;

// 1314151 James Sheaf-Morrison

public class NPStack {

    public static void main(String[] args) {
        // To help with testing fill in an empty args
        if (args.length == 0) {
            args = new String[1];
            args[0] = ("cubes0009.boxes");
        }

        if (args.length > 0) {
            try {

                BufferedReader textFile = new BufferedReader(new FileReader(args[0]));

                // Declare list to hold all the available boxes in
                List<Box> availBoxes;
                // Declare list to hold all the boxes in the stack
                List<Box> stackedBoxes;

                // Decalre variables to use while reading from file
                String textLine;
                String[] dimen;

                // Read in the boxes from file
                while (null != (textLine = regexpInput.readLine())) {
                    dimen = textLine.split(" ");
                    if (dimen.length > 3) {
                        Console.writeLine(
                                "Less than 3 dimensions given for a box in file" + args[0] + ". Line was " + textLine);
                    } else {
                        availBoxes.add(new Box(Integer.parseInt(dimen[0]), Integer.parseInt(dimen[1]),
                                Integer.parseInt(dimen[2])));
                    }
                }

                // Calculate which boxes to add to stack
                stackedBoxes = availBoxes;

                System.out.println("Stacked boxes from top at the top of list to the bottom at end of list:");
                // Display each box in stack
                for (Box b : stackedBoxes) {
                    System.out
                            .println(b.getFace(0).Width() + " " + b.getFace(0).Height() + " " + b.getFace(0).Height());
                }
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
                System.out.println("getHeight method given index of " + index + ", which was out of range");
                // By default return first face's height
                return uniqueFaces[0].Height();
            }
        }

        public List<Integer> facesThatFit(Face compare) {
            List<Integer> fittingFaces;
            // Check each unique face
            for (int i = 0; i < uniqueFaces.length; i++) {
                // If the width and height of this face fits
                if (uniqueFaces[i].fitsInto(compare)) {
                    fittingFaces.add(i);
                }
            }
            return fittingFaces;
        }
    }

    private static class Face {
        private int height;
        private int width;

        public Face(int height, int width) {
            this.height = height;
            this.width = width;
        }

        private int Height() {
            return height;
        }

        private int Width() {
            return width;
        }
    }
}
