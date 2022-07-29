package hello.servlet.web.frontController.v5;

import hello.servlet.web.frontController.ModelView;
import hello.servlet.web.frontController.MyView;
import hello.servlet.web.frontController.v3.Controller.MemberFormControllerV3;
import hello.servlet.web.frontController.v3.Controller.MemberListControllerV3;
import hello.servlet.web.frontController.v3.Controller.MemberSaveControllerV3;
import hello.servlet.web.frontController.v4.controller.MemberFormControllerV4;
import hello.servlet.web.frontController.v4.controller.MemberListControllerV4;
import hello.servlet.web.frontController.v4.controller.MemberSaveControllerV4;
import hello.servlet.web.frontController.v5.adapter.ControllerV3HandlerAdapter;
import hello.servlet.web.frontController.v5.adapter.ControllerV4HandlerAdapter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "frontControllerServletV5", urlPatterns = "/front-controller/v5/*")
public class FrontControllerServletV5 extends HttpServlet {

    private final Map<String, Object> handleMappingMap = new HashMap<>();
    private final List<MyHandlerAdapter> handlerAdapters = new ArrayList<>();


    public FrontControllerServletV5() {
        initHandlerMappingMap();
        initHandlerAdapters();
    }

    private void initHandlerMappingMap() {
        handleMappingMap.put("/front-controller/v5/v3/members/new-form", new MemberFormControllerV3());
        handleMappingMap.put("/front-controller/v5/v3/members/save", new MemberSaveControllerV3());
        handleMappingMap.put("/front-controller/v5/v3/members", new MemberListControllerV3());

        // v4 추가
        handleMappingMap.put("/front-controller/v5/v4/members/new-form", new MemberFormControllerV4());
        handleMappingMap.put("/front-controller/v5/v4/members/save", new MemberSaveControllerV4());
        handleMappingMap.put("/front-controller/v5/v4/members", new MemberListControllerV4());
    }

    private void initHandlerAdapters() {
        handlerAdapters.add(new ControllerV3HandlerAdapter());
        handlerAdapters.add(new ControllerV4HandlerAdapter());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 요청한 파라미터의 컨트롤러를 가져온다.
        Object handler = getHandler(req);

        if (handler == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        //해당하는 컨트롤러의 어댑터를 가져온다. ControllerV3HandlerAdapter()
        MyHandlerAdapter adpAdapter = getHandlerAdapter(handler);

        //파라미터를 전달하고, 핸들러(컨트롤러)를 전달해서 컨트롤러의 프로세스를 처리 한후 ModelView를 받는다.
        ModelView mv = adpAdapter.handle(req, resp, handler);

        // 뷰 리졸버를 통해서 물리데이터를 논리데이터로 변경한다.
        MyView myView = viewResolver(mv.getViewName());
        // 이후 데이터를 랜더링 한 후 화면에 뿌려준다.
        myView.render(mv.getModel(), req, resp);
    }


    private MyHandlerAdapter getHandlerAdapter(Object handler) {
        for (MyHandlerAdapter handlerAdapter : handlerAdapters) {
            if (handlerAdapter.supports(handler)) {
                return handlerAdapter;
            }
        }
        throw new IllegalArgumentException("handler adapter를 찾을 수 없습니다. handler=" + handler);
    }

    private Object getHandler(HttpServletRequest req) {
        String requestURI = req.getRequestURI();
        return handleMappingMap.get(requestURI);
    }

    private MyView viewResolver(String viewName) {
        return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    }
}
