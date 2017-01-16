package com.example.robert.drawingrobot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Iterator;

public class autoMode extends Activity {

    RelativeLayout relativeLayout;
    Paint paint;
    View view;
    Path path2;
    Bitmap bitmap;
    Canvas canvas;
    Button btnClear, btnConfirm;
    calculateTrajectory myTrajectory;
    int counter=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) { // tworzenie projektu
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_mode);

        relativeLayout = (RelativeLayout) findViewById(R.id.rLayout); // przypisywanie id do zmiennejj

        btnClear = (Button)findViewById(R.id.btnClear);
        btnConfirm = (Button)findViewById(R.id.btnConfirm);
        view = new SketchSheetView(autoMode.this); // obiekt o klasie view do rysowania

        myTrajectory=new calculateTrajectory();

        paint = new Paint(); // pedzel

        path2 = new Path();  // parametry do rysowania

        relativeLayout.addView(view, new LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        paint.setDither(true);

        paint.setColor(Color.parseColor("#0000FF"));

        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeJoin(Paint.Join.ROUND);

        paint.setStrokeCap(Paint.Cap.ROUND);

        paint.setStrokeWidth(4);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                path2.reset();
                myTrajectory.clear();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myTrajectory.calculate();
            }
        });

    }

    class SketchSheetView extends View {

        public SketchSheetView(Context context) {

            super(context); // wywolania konstruktora klasy nadrzedneh

            bitmap = Bitmap.createBitmap(820, 480, Bitmap.Config.ARGB_4444);

            canvas = new Canvas(bitmap); // plotno

            this.setBackgroundColor(Color.WHITE); // ustawianie koloru rysowania
        }

        private ArrayList<DrawingClass> DrawingClassArrayList = new ArrayList<DrawingClass>();

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            DrawingClass pathWithPaint = new DrawingClass();

            canvas.drawPath(path2, paint);

            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                path2.moveTo(event.getX(), event.getY());

                path2.lineTo(event.getX(), event.getY());

                Log.d("Pozycja", "pozycja x: " + event.getX() + "y: " + event.getY());

                myTrajectory.addElement(event.getX(), event.getY(),1);
            }
            else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                path2.lineTo(event.getX(), event.getY());
                if(counter>10) {
                    myTrajectory.addElement(event.getX(), event.getY(), 2);
                    counter=0;
                }
                pathWithPaint.setPath(path2);
                pathWithPaint.setPaint(paint);
                DrawingClassArrayList.add(pathWithPaint);
                counter++;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                counter=0;
                myTrajectory.addElement(event.getX(), event.getY(),0);
            }
            counter++;
            invalidate();
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (DrawingClassArrayList.size() > 0) {

                canvas.drawPath(
                        DrawingClassArrayList.get(DrawingClassArrayList.size() - 1).getPath(),

                        DrawingClassArrayList.get(DrawingClassArrayList.size() - 1).getPaint());
            }
        }
    }

    public class DrawingClass {

        Path DrawingClassPath;
        Paint DrawingClassPaint;

        public Path getPath() {
            return DrawingClassPath;
        }

        public void setPath(Path path) {
            this.DrawingClassPath = path;
        }


        public Paint getPaint() {
            return DrawingClassPaint;
        }

        public void setPaint(Paint paint) {
            this.DrawingClassPaint = paint;
        }
    }


    private class Location {
        float x;
        float y;
        int marker;
    }


    public class calculateTrajectory{ // klasa do przechowywania trajektorii i obliczania jej

        private ArrayList<Location> LocationList = new ArrayList<>();
        public ArrayList<String> CommandList = new ArrayList<>();
        public void addElement(float x, float y,int marker)
        {
            Location temp= new Location();
            temp.x=x;
            temp.y=y;
            temp.marker=marker;
            LocationList.add(temp);
        }
        public void clear()
        {
            LocationList.clear();
        }

        public void calculate()
        {
            for(int i=0;i<LocationList.size();i++)
            {
                float temp_distance = 0;
                float temp_angle = 0;
                float temp_angle2 = 0;
                float kat=0;
                if(LocationList.get(i).marker==0)
                    CommandList.add("U");
                if(LocationList.get(i).marker==1)
                    CommandList.add("D");
                if (i == 1) {
                    temp_distance = (float)Math.hypot(LocationList.get(i).x-LocationList.get(i-1).x, LocationList.get(i).y-LocationList.get(i-1).y);
                    CommandList.add("F"+(int)(temp_distance/10));
                    Log.d("Pozycja", "F"+(int)(temp_distance/10));
                }
                else if(i>=2)
                {
                    temp_angle=(float)Math.atan2(LocationList.get(i-1).y-LocationList.get(i).y,LocationList.get(i).x-LocationList.get(i-1).x)*180/3.14f;
                    temp_angle2=(float)Math.atan2(LocationList.get(i-2).y-LocationList.get(i-1).y,LocationList.get(i-1).x-LocationList.get(i-2).x)*180/3.14f;
                    kat=temp_angle2-temp_angle;
                    if(kat>180)
                        kat=-(360-kat);
                    if(kat<-180)
                        kat=360+kat;
                    if(kat<-4) {
                        CommandList.add("L" + (int) (-kat));
                        Log.d("Pozycja", "L" + (int) (-kat));

                    }
                    if(kat>4) {
                        CommandList.add("R" + (int) (kat));
                        Log.d("Pozycja", "R" + (int) (kat));

                    }
                    temp_distance = (float)Math.hypot(LocationList.get(i).x-LocationList.get(i-1).x, LocationList.get(i).y-LocationList.get(i-1).y);
                    CommandList.add("F"+(int)(temp_distance/10));
                    Log.d("Pozycja", "F"+(int)(temp_distance/10));
                }
            }


        }

    }
}


