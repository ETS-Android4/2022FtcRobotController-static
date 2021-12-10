package org.firstinspires.ftc.teamcode;
import static org.firstinspires.ftc.teamcode.Robot.basket;
import static org.firstinspires.ftc.teamcode.Robot.initAccessories;
import static org.firstinspires.ftc.teamcode.Robot.initMotors;
import static org.firstinspires.ftc.teamcode.Robot.slide;
import static org.firstinspires.ftc.teamcode.Robot.tablemotor;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

@Autonomous(name = "RED Left", preselectTeleOp = "teleopV2")
public class REDLeft extends LinearOpMode {

    OpenCvCamera webcam;
    final int START_X = -36;
    final int START_Y = -64;
    int level = 0;
    int height = 0;

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
        basket.setPosition(0.5);
        ////

        telemetry.addData("location: ", pipeline.getSide());
        telemetry.update();
        switch(pipeline.getSide()) {
            case LEFT_SIDE:
                level = 1;
                height = 0;
                break;
            case MIDDLE_SIDE:
                level = 2;
                height = -1100;
                break;
            case RIGHT_SIDE:
                level = 3;
                height = -1980;

        }
        Trajectory forward = drive.trajectoryBuilder(startPose) //Different start points
                .lineTo(new Vector2d(-36, -49))
                .build();
        drive.followTrajectory(forward);
        drive.turn(Math.toRadians(-90));
        drive.setPoseEstimate(new Pose2d(-36,-49, Math.toRadians(180)));
        //CHANGE ORIENT

        Trajectory toCarousel = drive.trajectoryBuilder(forward.end()) //Different start points
                .lineTo(new Vector2d(-30, -5))
                .build();
        drive.followTrajectory(toCarousel);
        drive.setPoseEstimate(new Pose2d(-30,-5, Math.toRadians(180)));

        Trajectory carouselAdjust = drive.trajectoryBuilder(toCarousel.end()) //Different start points
                .lineTo(new Vector2d(-38, 0))
                .build();
        drive.followTrajectory(carouselAdjust);

        tablemotor.setPower(-0.5);
        sleep(2500);
        tablemotor.setPower(0);

        Trajectory toTurn = drive.trajectoryBuilder(carouselAdjust.end())
                .lineTo(new Vector2d(-50, -64))
                .build();
        drive.followTrajectory(toTurn);

        drive.turn(Math.toRadians(-170));

        drive.setPoseEstimate(new Pose2d(-26,-64, Math.toRadians(0)));
        //CHANGE ORIENT


        Trajectory toShippingHub2Short = drive.trajectoryBuilder(toTurn.end())//Bottom
                .lineTo(new Vector2d(-46, -53))
                .build();
        Trajectory toShippingHub2Middle = drive.trajectoryBuilder(toTurn.end())//Middle
                .lineTo(new Vector2d(-44.5, -53))
                .build();
        Trajectory toShippingHub2Long = drive.trajectoryBuilder(toTurn.end())//Top
                .lineTo(new Vector2d(-44.25, -53))
                .build();

        if(level == 1) {
            drive.followTrajectory(toShippingHub2Short);
        } else if(level == 2) {
            drive.followTrajectory(toShippingHub2Middle);
        } else {
            drive.followTrajectory(toShippingHub2Long);
        }

        //Deliver

        slide.setTargetPosition(height);
        slide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slide.setPower(0.6);
        while(slide.isBusy()){}

        basket.setPosition(0.93);
        sleep(4000);
        basket.setPosition(0.48);

        slide.setTargetPosition(0);
        slide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slide.setPower(0.6);

        Trajectory align;
        Trajectory sprint;
        if(level == 1) {
            align = drive.trajectoryBuilder(toShippingHub2Middle.end()) //Different start points
                    .lineTo(new Vector2d(-37, -57))
                    .build();
            drive.followTrajectory(align);

            //CHNGE ORIENT
            drive.turn(Math.toRadians(-179));
            drive.setPoseEstimate(new Pose2d(-37,-57, Math.toRadians(180)));

            sprint = drive.trajectoryBuilder(align.end()) //Different start points
                    .lineTo(new Vector2d(-37, -137))
                    .build();
            drive.followTrajectory(sprint);
        } else if(level == 2) {
            align= drive.trajectoryBuilder(toShippingHub2Middle.end()) //Different start points
                    .lineTo(new Vector2d(-37, -57))
                    .build();
            drive.followTrajectory(align);

            //CHNGE ORIENT
            drive.turn(Math.toRadians(-177));
            drive.setPoseEstimate(new Pose2d(-37,-57, Math.toRadians(180)));

            sprint = drive.trajectoryBuilder(align.end()) //Different start points
                    .lineTo(new Vector2d(-37, -137))
                    .build();
            drive.followTrajectory(sprint);
        } else {
            align = drive.trajectoryBuilder(toShippingHub2Long.end()) //Different start points
                    .lineTo(new Vector2d(-37, -57))
                    .build();
            drive.followTrajectory(align);

            drive.turn(Math.toRadians(-177));
            drive.setPoseEstimate(new Pose2d(-37,-57, Math.toRadians(180)));

            sprint = drive.trajectoryBuilder(align.end()) //Different start points
                    .lineTo(new Vector2d(-37, -137))
                    .build();
            drive.followTrajectory(sprint);
        }

        if (isStopRequested()) return;
        sleep(2000);

    }
}
