package testexample.currencyapiexample.component;

import javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return (
                response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                || response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR
                );
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException{
        if(response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR){
            //handle server error
        } else if (response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR){
            //handle client error
            if(response.getStatusCode() == HttpStatus.NOT_FOUND){
                try {
                    throw new NotFoundException("Not found exception");
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
