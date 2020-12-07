package SaverchenkoGroup10Lab5VarC;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

public class GraphicsDisplay extends JPanel {

    private Double[][] graphicsData;

    private boolean showAxis = true;
    private boolean showDefaultCondition = false;
    private boolean showModifiedCondition = false;
    private boolean showGrid = false;
    private boolean turnGraph = false;

    private final BasicStroke axisStroke;
    private final BasicStroke modifiedGraphicsStroke;
    private final BasicStroke markerStroke;
    private final BasicStroke graphicsStroke;
    private final BasicStroke gridStroke;
    private final BasicStroke gridStrokeMin;

    private final Font axisFont;
    private final Font gridFont;

    private Double minX;
    private Double maxX;
    private Double minY;
    private Double maxY;

    private double scale;
    private double scaleX;
    private double scaleY;

    private final DecimalFormat formatterX = (DecimalFormat) NumberFormat.getInstance();
    private final DecimalFormat formatterY = (DecimalFormat) NumberFormat.getInstance();

    public GraphicsDisplay () {
        setBackground(Color.WHITE);
        graphicsStroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        axisStroke = new BasicStroke(2f,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        axisFont = new Font(Font.SANS_SERIF, Font.PLAIN+Font.ITALIC, 36);
        modifiedGraphicsStroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                10f,new float[] {18,5,5,5,12,5,5,5}, 0f);
        markerStroke = new BasicStroke(2f);
        gridStroke = new BasicStroke(1f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,
                10f, new float[]{1}, 0f);
        gridFont = new Font(Font.SANS_SERIF, Font.ITALIC, 18);
        gridStrokeMin = new BasicStroke(1f);

        formatterX.setGroupingUsed(false);
        formatterY.setGroupingUsed(false);
        DecimalFormatSymbols dottedDoubleX = formatterX.getDecimalFormatSymbols();
        DecimalFormatSymbols dottedDoubleY = formatterY.getDecimalFormatSymbols();
        dottedDoubleX.setDecimalSeparator('.');
        formatterX.setDecimalFormatSymbols(dottedDoubleX);
        dottedDoubleY.setDecimalSeparator('.');
        formatterY.setDecimalFormatSymbols(dottedDoubleY);
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setDefaultCondition(boolean showDefaultCondition) {
        this.showDefaultCondition = showDefaultCondition;
        repaint();
    }

    public void setModifiedCondition(boolean showModifiedCondition) {
        this.showModifiedCondition = showModifiedCondition;
        repaint();
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
    }

    public void setTurnGraph(boolean turnGraph) {
        this.turnGraph = turnGraph;
        repaint();
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length ==0 ) return;

        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length-1][0];
        minY = graphicsData[0][1];
        maxY = minY;

        for (int i = 1; i<graphicsData.length; i++) {
            if (graphicsData[i][1]<minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1]>maxY) {
                maxY = graphicsData[i][1];
            }
        }

        if (turnGraph) {
            scaleX = getSize().getHeight() / (maxX - minX);
            scaleY = getSize().getWidth() / (maxY - minY);
            scale = Math.min(scaleX, scaleY);

            if (scale == scaleY) {
                double xIncrement = (getSize().getHeight()/scale - (maxX - minX))/2;
                maxX += xIncrement;
                minX -= xIncrement;
            }

            if (scale == scaleX) {
                double yIncrement = (getSize().getWidth()/scale - (maxY - minY))/2;
                maxY += yIncrement;
                minY -= yIncrement;
            }
        }
        else {
            scaleX = getSize().getWidth() / (maxX - minX);
            scaleY = getSize().getHeight() / (maxY - minY);
            scale = Math.min(scaleX, scaleY);

            if (scale == scaleY) {
                double xIncrement = (getSize().getWidth()/scale - (maxX - minX))/2;
                maxX += xIncrement;
                minX -= xIncrement;
            }

            if (scale == scaleX) {
                double yIncrement = (getSize().getHeight()/scale - (maxY - minY))/2;
                maxY += yIncrement;
                minY -= yIncrement;
            }
        }

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();

        if (turnGraph) rotatePanel(canvas);
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        if (showDefaultCondition) paintMarkers(canvas);
        if (showGrid) paintGrids(canvas);

        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);

    }

    protected void paintMarkers (Graphics2D canvas) {

        canvas.setStroke(markerStroke);

        for (Double[] point : graphicsData) {
            GeneralPath marker = new GeneralPath();
            if (showModifiedCondition && point[1].intValue()%2==0 && point[1].intValue()!=0)
                canvas.setColor(Color.red);
            else
                canvas.setColor(Color.blue);
            Point2D.Double center = xyToPoint(point[0],point[1]);
            marker.moveTo(center.getX() + 2.75, center.getY() - 5);
            marker.lineTo(marker.getCurrentPoint().getX() - 5.5, marker.getCurrentPoint().getY());
            marker.moveTo(marker.getCurrentPoint().getX(), marker.getCurrentPoint().getY() + 10);
            marker.lineTo(marker.getCurrentPoint().getX() + 5.5, marker.getCurrentPoint().getY());
            marker.moveTo(center.getX(), marker.getCurrentPoint().getY());
            marker.lineTo(marker.getCurrentPoint().getX(), marker.getCurrentPoint().getY() - 10);
            marker.moveTo(center.getX() - 5, center.getY() + 2.75);
            marker.lineTo(marker.getCurrentPoint().getX(), marker.getCurrentPoint().getY() - 5.5);
            marker.moveTo(marker.getCurrentPoint().getX() + 10, marker.getCurrentPoint().getY());
            marker.lineTo(marker.getCurrentPoint().getX(), marker.getCurrentPoint().getY() + 5.5);
            marker.moveTo(marker.getCurrentPoint().getX(), center.getY());
            marker.lineTo(marker.getCurrentPoint().getX() - 10, marker.getCurrentPoint().getY());
            canvas.draw(marker);
        }
        repaint();
    }

    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.black);
        canvas.setPaint(Color.black);
        canvas.setFont(axisFont);
        FontRenderContext context = canvas.getFontRenderContext();
        if (minX<=0.0 && maxX>=0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 14);
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
            canvas.drawString("y", (float)labelPos.getX() + 10, (float)(labelPos.getY() - bounds.getY()));
        }
        if (minY<=0.0 && maxY>=0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() - 14, arrow.getCurrentPoint().getY() - 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
            canvas.drawString("x", (float)(labelPos.getX()-bounds.getWidth()-10), (float)(labelPos.getY() + bounds.getY()));

        }
    }

    protected void paintGraphics(Graphics2D canvas) {

        if (showDefaultCondition) {
            canvas.setStroke(modifiedGraphicsStroke);
            canvas.setColor(Color.green.darker());
        }
        else {
            canvas.setStroke(graphicsStroke);
            canvas.setColor(Color.red.brighter().brighter());
        }

        GeneralPath graphics = new GeneralPath();
        for (int i=0; i<graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0],
                    graphicsData[i][1]);
            if (i>0) {
                graphics.lineTo(point.getX(), point.getY());
            } else {
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        canvas.draw(graphics);
    }

    protected void rotatePanel(Graphics2D canvas){
        canvas.translate(0, getHeight());
        canvas.rotate(-Math.PI/2);
    }

    protected void paintGrids(Graphics2D canvas){
        canvas.setFont(gridFont);
        FontRenderContext context = canvas.getFontRenderContext();
        canvas.setColor(Color.gray);
        double currentValueX=0;
        double currentValueY=0;
        double incrementX = (maxX-minX)/20;
        double incrementY = (maxY-minY)/20;
        double incrementXIn = Double.parseDouble(formatterX.format(incrementX))/10;
        double incrementYIn = Double.parseDouble(formatterY.format(incrementY))/10;
        double currentValueXModified = -Double.parseDouble(formatterX.format(incrementX));
        double currentValueYModified = -Double.parseDouble(formatterY.format(incrementY));
        int counter;
        double currentValueXIn;
        double currentValueYIn;

        while((currentValueX<maxX || currentValueY<maxY) || (-currentValueX>minX || -currentValueY>minY)) {
            canvas.setStroke(gridStroke);
            String formattedDoubleX = formatterX.format(currentValueX);
            String formattedDoubleY = formatterY.format(currentValueY);
            canvas.draw(new Line2D.Double(xyToPoint(currentValueX,minY),xyToPoint(currentValueX,maxY)));
            canvas.draw(new Line2D.Double(xyToPoint(-currentValueX,minY),xyToPoint(-currentValueX,maxY)));
            canvas.draw(new Line2D.Double(xyToPoint(minX,currentValueY),xyToPoint(maxX,currentValueY)));
            canvas.draw(new Line2D.Double(xyToPoint(minX,-currentValueY),xyToPoint(maxX,-currentValueY)));
            Rectangle2D boundsX = gridFont.getStringBounds(formattedDoubleX,context);
            Rectangle2D boundsY = gridFont.getStringBounds(formattedDoubleY,context);
            Point2D.Double labelPosXRight = xyToPoint(-currentValueXModified,0);
            Point2D.Double labelPosXLeft = xyToPoint(currentValueXModified,0);
            Point2D.Double labelPosYUp = xyToPoint(0,currentValueY);
            Point2D.Double labelPosYDown = xyToPoint(0,currentValueYModified);
            canvas.drawString(formatterX.format(-currentValueXModified),(float)(labelPosXRight.getX()-15),
                    (float)(labelPosXRight.getY())-5);
            canvas.drawString(formatterX.format(currentValueXModified),(float)(labelPosXLeft.getX()-boundsX.getX()-15),
                    (float)(labelPosXLeft.getY())-5);
            canvas.drawString(formatterY.format(currentValueY),(float)(labelPosYUp.getX()-boundsY.getX()+10),
                    (float)(labelPosYUp.getY()-boundsY.getY()));
            canvas.drawString(formatterY.format(currentValueYModified),(float)(labelPosYDown.getX()-boundsY.getX()+10),
                    (float)(labelPosYDown.getY()-boundsY.getY()));
            currentValueYIn=0;
            counter=0;
            canvas.setStroke(gridStrokeMin);
            while(currentValueYIn<=maxY || -currentValueYIn>minY) {
                if ((counter+15)%10==0) {
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueX, currentValueYIn), -6, 0),
                            shiftPoint(xyToPoint(currentValueX, currentValueYIn), 6, 0)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueX, -currentValueYIn), -6, 0),
                            shiftPoint(xyToPoint(currentValueX, -currentValueYIn), 6, 0)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueX, currentValueYIn), -6, 0),
                            shiftPoint(xyToPoint(-currentValueX, currentValueYIn), 6, 0)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueX, -currentValueYIn), -6, 0),
                            shiftPoint(xyToPoint(-currentValueX, -currentValueYIn), 6, 0)));
                }
                else
                {
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueX, currentValueYIn),-3,0),
                            shiftPoint(xyToPoint(currentValueX, currentValueYIn),3,0)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueX, -currentValueYIn),-3,0),
                            shiftPoint(xyToPoint(currentValueX, -currentValueYIn),3,0)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueX, currentValueYIn), -3, 0),
                            shiftPoint(xyToPoint(-currentValueX, currentValueYIn), 3, 0)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueX, -currentValueYIn), -3, 0),
                            shiftPoint(xyToPoint(-currentValueX, -currentValueYIn), 3, 0)));
                }
                counter++;
                currentValueYIn+=incrementYIn;
            }

            counter=0;
            currentValueXIn=0;
            while(currentValueXIn<=maxX || -currentValueXIn>minX) {
                if ((counter+15)%10==0) {
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueXIn, currentValueY), 0, -6),
                            shiftPoint(xyToPoint(currentValueXIn, currentValueY), 0, 6)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueXIn, -currentValueY), 0, -6),
                            shiftPoint(xyToPoint(currentValueXIn, -currentValueY), 0, 6)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueXIn, currentValueY), 0, -6),
                            shiftPoint(xyToPoint(-currentValueXIn, currentValueY), 0, 6)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueXIn, -currentValueY), 0, -6),
                            shiftPoint(xyToPoint(-currentValueXIn, -currentValueY), 0, 6)));
                }
                else
                {
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueXIn, currentValueY), 0, -3),
                            shiftPoint(xyToPoint(currentValueXIn, currentValueY), 0, 3)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueXIn, -currentValueY), 0, -3),
                            shiftPoint(xyToPoint(currentValueXIn, -currentValueY), 0, 3)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueXIn, currentValueY), 0, -3),
                            shiftPoint(xyToPoint(-currentValueXIn, currentValueY), 0, 3)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueXIn, -currentValueY), 0, -3),
                            shiftPoint(xyToPoint(-currentValueXIn, -currentValueY), 0, 3)));
                }
                counter++;
                currentValueXIn+=incrementXIn;
            }

            currentValueY += Double.parseDouble(formatterY.format(incrementY));
            currentValueX += Double.parseDouble(formatterX.format(incrementX));
            currentValueXModified -= Double.parseDouble(formatterX.format(incrementX));
            currentValueYModified -= Double.parseDouble(formatterY.format(incrementY));
        }
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX*scale, deltaY*scale);
    }

    public void setXDigits(int x){
        formatterX.setMaximumFractionDigits(x);
    }

    public void setYDigits(int y){
        formatterY.setMaximumFractionDigits(y);
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }

    public String getIncrX() {
        return Double.toString((maxX-minX)/20);
    }

    public String getIncrY() {
        return Double.toString((maxY-minY)/20);
    }

    public Double getIncrXDouble() {
        return (maxX-minX)/20;
    }

    public Double getIncrYDouble() {
        return (maxY-minY)/20;
    }
}
