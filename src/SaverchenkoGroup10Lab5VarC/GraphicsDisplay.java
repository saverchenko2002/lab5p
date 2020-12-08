package SaverchenkoGroup10Lab5VarC;

import jdk.jfr.Description;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Stack;


public class GraphicsDisplay extends JPanel {

    private Double[][] graphicsData;

    private Double[][] graphicsDataOriginal; //Матрица для сохранения(если вдруг изменились точки)
    public Stack<Double[][]> undoLog = new Stack<>(); //Стэк для хранения всех приближений и отката к первоначальному виду
    private int selectedMarker = -1; //когда навелись мышкой на маркер меняет значение
    private Double[][] viewport = new Double[2][2]; //Границы окошка сюда сохраняются начинаня с текущего
    Double[] originalPoint = new Double[2]; //этот и нижний массив для временного хранения границ при выделении
    Double[] finalPoint = new Double[2];
    boolean scaleMode = false; //для отрисовки выделительного прямоугольника
    boolean changeMode = false;// для перемещения точек
    boolean changes = false;
    private final java.awt.geom.Rectangle2D.Double selectionRect = new java.awt.geom.Rectangle2D.Double(); //сам прямоугольник
    private static DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance();

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
    private final BasicStroke selectionStroke;

    private final Font axisFont;
    private final Font labelFont; //шрифт для окошка с координатами при наведении на точку

    private double minX, maxX, minY, maxY; //оставил для более простого отката к резету
    private double scaleX;
    private double scaleY;

    public GraphicsDisplay() {
        setBackground(Color.WHITE);
        selectionStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[]{10.0F, 10.0F}, 0.0F); //строка для прямоугольника
        graphicsStroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        axisStroke = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        axisFont = new Font(Font.SANS_SERIF, Font.PLAIN + Font.ITALIC, 36);
        modifiedGraphicsStroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                10f, new float[]{18, 5, 5, 5, 12, 5, 5, 5}, 0f);
        markerStroke = new BasicStroke(2f);
        gridStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                10f, new float[]{1}, 0f);
        labelFont = new Font(Font.SERIF, Font.PLAIN+Font.ITALIC, 18);

        addMouseListener(new MouseHandler());
        addMouseMotionListener(new MouseMotionHandler());
    }

    @Description("Тут мы инициализируем массив для резета")
    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        graphicsDataOriginal = new Double[graphicsData.length][2];
        for(int i=0; i<graphicsData.length; i++) {
            graphicsDataOriginal[i][0]=graphicsData[i][0];
            graphicsDataOriginal[i][1]=graphicsData[i][1];
        }
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

    public void zoomToRegion(double x1, double y1, double x2, double y2) {  //для сохранения пределов нового окна

        viewport[0][0]=x1;
        viewport[0][1]=y1;
        viewport[1][0]=x2;
        viewport[1][1]=y2;

        repaint();
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length == 0) return;

        if(undoLog.size()==0) {
            viewport[0][0] = graphicsData[0][0];
            viewport[1][0] = graphicsData[graphicsData.length - 1][0];
            viewport[1][1] = graphicsData[0][1];
            viewport[0][1] = viewport[1][1];

        /*
        оперируем viewport таким образом чтобы viewport
        [1][0] - maxX;
        [1][1] - minY;
        [0][0] - minX;
        [0][1] - maxY
         */
            for (int i = 1; i < graphicsData.length; i++) {
                if (graphicsData[i][1] < viewport[1][1]) {
                    viewport[1][1] = graphicsData[i][1];
                }
                if (graphicsData[i][1] > viewport[0][1]) {
                    viewport[0][1] = graphicsData[i][1];
                }
            }

            maxX = viewport[1][0];
            minX = viewport[0][0];
            maxY = viewport[0][1];
            minY = viewport[1][1];
        }

        if (turnGraph) {
            scaleX = getSize().getHeight() / (viewport[1][0] - viewport[0][0]);
            scaleY = getSize().getWidth() / (viewport[0][1] - viewport[1][1]);
            double scale = Math.min(scaleX, scaleY);

           if (scale == scaleY) {
                double xIncrement = (getSize().getHeight() / scaleX - (viewport[1][0] - viewport[0][0])) / 2;
                viewport[1][0] += xIncrement;
                viewport[0][0] -= xIncrement;
            }

            if (scale == scaleX) {
                double yIncrement = (getSize().getWidth() / scaleY - (viewport[0][1] - viewport[1][1])) / 2;
                viewport[0][1] += yIncrement;
                viewport[1][1] -= yIncrement;
            }
        } else {
            scaleX = getSize().getWidth() / (viewport[1][0] - viewport[0][0]);
            scaleY = getSize().getHeight() / (viewport[0][1] - viewport[1][1]);
            double scale = Math.min(scaleX, scaleY);

          if (scale == scaleY) {
                double xIncrement = (getSize().getWidth() / scaleX - (viewport[1][0] - viewport[0][0])) / 2;
                viewport[1][0] += xIncrement;
                viewport[0][0] -= xIncrement;
            }

            if (scale == scaleX) {
                double yIncrement = (getSize().getHeight() / scaleY - (viewport[0][1] - viewport[1][1])) / 2;
                viewport[0][1] += yIncrement;
                viewport[1][1] -= yIncrement;
            }
        }

        Graphics2D canvas = (Graphics2D) g;

        if (turnGraph) rotatePanel(canvas);
        if (showGrid) paintGrids(canvas);
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        if (showDefaultCondition) paintMarkers(canvas);
        paintSelection(canvas);

    }

    private void paintSelection(Graphics2D canvas) {
        if (scaleMode) {
            canvas.setStroke(selectionStroke);
            canvas.setColor(Color.black);
            canvas.draw(selectionRect);
        }
    }

    protected void reset() {

        if (graphicsData != null)
            for (int i = 0; i < graphicsData.length; i++) {
                graphicsData[i][0] = graphicsDataOriginal[i][0];
                graphicsData[i][1] = graphicsDataOriginal[i][1];
            }

        zoomToRegion(minX,maxY,maxX,minY);
    }

    protected void paintMarkers(Graphics2D canvas) {

        canvas.setStroke(markerStroke);

        for (Double[] point : graphicsData) {
            GeneralPath marker = new GeneralPath();
            if (showModifiedCondition && point[1].intValue() % 2 == 0 && point[1].intValue() != 0)
                canvas.setColor(Color.red);
            else
                canvas.setColor(Color.blue);
            Point2D.Double center = xyToPoint(point[0], point[1]);
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
        if (viewport[0][0] <= 0.0 && viewport[1][0] >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(0, viewport[0][1]), xyToPoint(0, viewport[1][1])));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, viewport[0][1]);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 14);
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, viewport[0][1]);
            canvas.drawString("y", (float) labelPos.getX() + 10, (float) (labelPos.getY() - bounds.getY()));
        }
        if (viewport[1][1] <= 0.0 && viewport[0][1] >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(viewport[0][0], 0), xyToPoint(viewport[1][0], 0)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(viewport[1][0], 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() - 14, arrow.getCurrentPoint().getY() - 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(viewport[1][0], 0);
            canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10), (float) (labelPos.getY() + bounds.getY()));

        }
    }

    protected void paintGraphics(Graphics2D canvas) {

        if (showDefaultCondition) {
            canvas.setStroke(modifiedGraphicsStroke);
            canvas.setColor(Color.green.darker());
        } else {
            canvas.setStroke(graphicsStroke);
            canvas.setColor(Color.red.brighter().brighter());
        }

        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0],
                    graphicsData[i][1]);
            if (i > 0) {
                graphics.lineTo(point.getX(), point.getY());
            } else {
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        canvas.draw(graphics);
    }

    protected void rotatePanel(Graphics2D canvas) {
        canvas.translate(0, getHeight());
        canvas.rotate(-Math.PI / 2);
    }

    protected void paintGrids(Graphics2D canvas) {

        canvas.setStroke(gridStroke);
        canvas.setColor(Color.gray.darker());

        double pos = this.viewport[0][0];
        double step;
        for(step = (viewport[1][0] - viewport[0][0]) / 10.0D; pos < viewport[1][0]; pos += step) {
            canvas.draw(new Line2D.Double(xyToPoint(pos, viewport[0][1]), xyToPoint(pos, viewport[1][1])));
        }

        canvas.draw(new Line2D.Double(xyToPoint(viewport[1][0], viewport[0][1]), xyToPoint(viewport[1][0], viewport[1][1])));
        pos = viewport[1][1];

        for(step = (viewport[0][1] - viewport[1][1]) / 10.0D; pos < viewport[0][1]; pos += step) {
            canvas.draw(new Line2D.Double(xyToPoint(viewport[0][0], pos), xyToPoint(viewport[1][0], pos)));
        }

        canvas.draw(new Line2D.Double(xyToPoint(viewport[0][0], viewport[0][1]), xyToPoint(viewport[1][0], viewport[0][1])));

        canvas.setColor(Color.BLACK);
        canvas.setFont(labelFont);
        FontRenderContext context = canvas.getFontRenderContext();

        double labelY;
        if (viewport[1][1] < 0.0 &&  0.0 < viewport[0][1] && showAxis)
            labelY = 0.0;
        else
            labelY = viewport[1][1];

        double labelX;
        if (viewport[0][0] < 0.0 && 0.0 < viewport[1][0] && showAxis)
            labelX = 0.0;
        else
            labelX = viewport[0][0];

        pos = viewport[0][0];
        Point2D.Double point;
        String label;
        Rectangle2D bounds;
        formatter.setMaximumFractionDigits(undoLog.size()+1);
        for (step = (viewport[1][0] - viewport[0][0]) / 10.0; pos < viewport[1][0]; pos += step){
            point = xyToPoint(pos, labelY);
            label = formatter.format(pos);
            bounds = labelFont.getStringBounds(label, context);
            canvas.drawString(label, (float)(point.getX() +5), (float)(point.getY() - bounds.getHeight()));
        }

        pos = viewport[1][1];
        for (step = (viewport[0][1] - viewport[1][1]) / 10.0; pos < viewport[0][1]; pos += step){
            point = xyToPoint(labelX, pos);
            label = formatter.format(pos);
            bounds = labelFont.getStringBounds(label, context);
            canvas.drawString(label, (float)(point.getX() +5.0), (float)(point.getY() - bounds.getHeight()));
        }

    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - viewport[0][0];
        double deltaY = viewport[0][1] - y;
        return new Point2D.Double(deltaX * scaleX, deltaY * scaleY);
    }

    protected Double[] pointToXY(int x, int y) {
        return new Double[]{viewport[0][0]+(double)x/scaleX, viewport[0][1]-(double)y/scaleY};
    }

    protected int findPoint(int x, int y) {
        if (graphicsData != null) {
            int pos = 0;
            for (Double[] point : graphicsData) {
                Point2D.Double screenPoint = xyToPoint(point[0], point[1]);
                double distance = (screenPoint.getX() - x) * (screenPoint.getX() - x) +
                        (screenPoint.getY() - y) * (screenPoint.getY() - y);
                if (distance < 100.0)
                    return pos;
                pos++;
            }
        }
        return -1;
    }

    public Double[][] getGraphicsData(){
        return graphicsData;
    }

    public class MouseHandler extends MouseAdapter {

        public void mouseClicked(MouseEvent e) { //При щелчке мышью вызывается метод
            if (e.getButton() == 3) {//пкм клавиша значит
                if (!undoLog.isEmpty())
                    viewport = undoLog.pop();
                else
                    zoomToRegion(viewport[0][0],viewport[0][1],viewport[1][0],viewport[1][1]);
                repaint();
            }
        }

        @Description("Для забора начальных координат при приближении")
        public void mousePressed(MouseEvent e) { //при нажатии
            if (e.getButton() == 1) {
                selectedMarker = findPoint(e.getX(), e.getY());
                originalPoint = pointToXY(e.getX(), e.getY() - getHeight());
                if (selectedMarker >= 0) {
                    changeMode = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                } else {

                    scaleMode = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                    selectionRect.setFrame(e.getX(), e.getY(), 25.0D, 25.0D);
                }
            }


        }

        @Description("Для забора координат при приближении")
        public void mouseReleased(MouseEvent e) { // при отпускании кнопки мыши
            if (e.getButton() == 1) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                if (changeMode){
                    changeMode = false;
                    selectedMarker = -1;
                    repaint();
                }
                else {
                    scaleMode = false;
                    finalPoint = pointToXY(e.getX(), e.getY());
                    undoLog.add(viewport);
                    viewport = new Double[2][2];
                    zoomToRegion(originalPoint[0], originalPoint[1], finalPoint[0], finalPoint[1]);
                }
            }
        }

    }

    public class MouseMotionHandler implements MouseMotionListener {

        public void mouseDragged(MouseEvent e) { //многократно вызывается при перетаскивании мыши, когда нажата ее левая кнопка
            if (changeMode) {
                Double[] currentPoint = pointToXY(e.getX(), e.getY());
                double newY = currentPoint[1];
                if (newY > viewport[0][1])
                    newY = viewport[0][1];
                if (newY < viewport[1][1])
                    newY = viewport[1][1];
                graphicsData[selectedMarker][1] = newY;
                changes = true;
            } else {
                double width = e.getX() - selectionRect.getX();
                double height = e.getY() - selectionRect.getY();
                selectionRect.setFrame(selectionRect.getX(), selectionRect.getY(), width, height);
            }
            repaint();
        }

        public void mouseMoved(MouseEvent e) { //Обычное перемещение мыши приводит к  многократному вызову метода
            selectedMarker = findPoint(e.getX(),e.getY());
            if (selectedMarker>=0)
                setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            else
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            repaint();
        }
    }
}

