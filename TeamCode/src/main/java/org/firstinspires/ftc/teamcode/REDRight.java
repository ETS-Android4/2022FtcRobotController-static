package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.Robot.RunIntake;
import static org.firstinspires.ftc.teamcode.Robot.basket;
import static org.firstinspires.ftc.teamcode.Robot.initAccessories;
import static org.firstinspires.ftc.teamcode.Robot.initMotors;
import static org.firstinspires.ftc.teamcode.Robot.slide;
import static org.firstinspires.ftc.teamcode.Robot.tablemotor;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

@Autonomous(name = "RedRight", preselectTeleOp = "teleopV2")
public class REDRight extends LinearOpMode{
    OpenCvCamera webcam;
    final int START_X = 36;
    final int START_Y = -64;
    int level = 0;
    int height = 0;
    double basket_value;

    @Override
    public void runOpMode() throws InterruptedException {
        ////////////init camera and motors ////////////////////////////////////////////////////////////////////
        telemetry.addLine("Not initialized");
        telemetry.update();

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        initMotors(this);
        initAccessories(this);

        Pose2d startPose = new Pose2d(START_X, START_Y, Math.toRadians(-90)); //init starting position
        drive.setPoseEstimate(startPose);

        //////Start Camera Streaming//////
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam"), cameraMonitorViewId);


        ConeVisionPipeline pipeline = new ConeVisionPipeline(telemetry);
        webcam.setPipeline(pipeline);

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {
                telemetry.addData("Error", errorCode);
                telemetry.update();
            }
        });


////////Program start////////////////////////////////////////////////////////////////////////

        waitForStart();
        ////Move on start/init
        basket.setPosition(Robot.basketdefault);
        ////

        switch(pipeline.getSide()) {
            case LEFT_SIDE:
                level = 1;
                height = 0;
                basket_value = 0.92;
                break;
            case MIDDLE_SIDE:
                level = 2;
                height = 1350;
                basket_value = 0.95;
                break;
            case RIGHT_SIDE:
                level = 3;
                height = 2050;
                basket_value = 0.93;

        }

        double added = 0;
        if (level == 2) added = 2;
        else if (level == 3) added = 5.5;
        Trajectory deliverPreload = drive.trajectoryBuilder(startPose) //moves bot forward from start and turns
               .lineToLinearHeading(new Pose2d(67.5, -38 + added, Math.toRadians(-15)))
               .build();

        Trajectory shippingToWall = drive.trajectoryBuilder(deliverPreload.end()) //moves to
                .lineTo(new Vector2d(50, -65))
                .build();

        Trajectory toWarehouse = drive.trajectoryBuilder(shippingToWall.end())
                .back(42)
                .build();

        Trajectory warehouseToWall = drive.trajectoryBuilder(toWarehouse.end())
                .forward(68)
                .build();
        Trajectory toHub = drive.trajectoryBuilder(warehouseToWall.end())
                .lineTo(new Vector2d(76, -44))
                .build();



        drive.followTrajectory(deliverPreload);

        slide.setTargetPosition(height);
        slide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slide.setPower(0.6);
        while(slide.isBusy()){}


        basket.setPosition(basket_value);
        sleep(3000);
        basket.setPosition(Robot.basketdefault);

        slide.setTargetPosition(0);
        slide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slide.setPower(0.6);

        height = 2050;
        basket_value = 0.93;
        for(int i = 0; i < 1; i++) {
            //changes how far the robot goes foward to account for ball and block pushing
            Trajectory toWarehouseLong = drive.trajectoryBuilder(shippingToWall.end())
                    .back(45 + (i * 5))
                    .build();
            //changes how far the robot goes back to account for ball and block pushing
            Trajectory warehouseToWallLong = drive.trajectoryBuilder(toWarehouse.end())
                    .forward(60 + (i * 5))
                    .build();
            Trajectory align = drive.trajectoryBuilder(toWarehouseLong.end())
                    .strafeRight(11.2)
                    .build();


            drive.followTrajectory(shippingToWall);

            RunIntake(-.90);


            drive.followTrajectory(toWarehouseLong);

            sleep(1000);
            drive.followTrajectory(align);
            RunIntake(.90);
            slide.setTargetPosition(height);
            slide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            slide.setPower(0.4);


            drive.followTrajectory(warehouseToWallLong);

            RunIntake(0);
            drive.followTrajectory(toHub);
            while(slide.isBusy()){}

            basket.setPosition(basket_value);
            sleep(2500);
            basket.setPosition(Robot.basketdefault);

            slide.setTargetPosition(0);
            slide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            slide.setPower(0.6);
        }
        drive.followTrajectory(shippingToWall);
        drive.followTrajectory(toWarehouse);

        if (isStopRequested()) return;
        sleep(2000);


    }
}
