package lvgnll.chatsample;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;

public interface LamInter {
    @LambdaFunction
    LamRes pusherAgent(LamReq request);
}
