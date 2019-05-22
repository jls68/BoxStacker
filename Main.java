import java.list;

// 1314151 James Sheaf-Morrison

public class Main {

    public static void main(String[] args) {
        // Declare list to hold all the available boxes in
        List<Box> availBoxes;
    }

    private static bool CanBeOnTop(Point bottom, Point top){
        // Check if the width and height of the top box's face is less than that of the bottom box's relative feature
        if(top.getX() < bottom.getX() && top.getY() < bottem.getY()){
            return true;
        }
        return false;
    }

    private class Box {
        // Using a list of points as it offers an X and a Y value which is all we need
        priavte List<Point> uniqueFaces;

        public Box(int width, int height, int depth){
            uniqueFaces.add(new Point(width, height));

            // If depth is not the same as width and height
            if(width != height && width != depth){
                // Then add another face to list
                uniqueFaces.add(new Point(depth, height));
            }

            // If width is not the same as depth and height
            if(width != height && heighth != depth){
                // Then add another face to list
                uniqueFaces.add(new Point(depth, width));
            }
        }
    }
}
