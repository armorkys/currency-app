package testexample.currencyapiexample.controller;


import lt.lb.webservices.fxrates.CcyISO4217;
import lt.lb.webservices.fxrates.FxRatesHandling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import testexample.currencyapiexample.model.CcyComparator;
import testexample.currencyapiexample.model.DateHistoryTemplate;
import testexample.currencyapiexample.service.MainService;

import javax.validation.Valid;
import javax.xml.datatype.DatatypeConfigurationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Controller
public class MainController {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private MainService service;

    @GetMapping("/")
    public String getCurrentRatesList(Model model) throws DatatypeConfigurationException {
        model.addAttribute("currencyList", service.getCurrentCurrencyRates().getFxRate());
        return "index";
    }

    @GetMapping("/getCurrencyHistory/{ccy}")
    public String getCurrencyHistory(@PathVariable(value = "ccy") CcyISO4217 ccy,
                                     Model model) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(1);
        FxRatesHandling historyList = service.getCurrencyHistoryBase(ccy.toString(), startDate.toString(), endDate.toString());
        DateHistoryTemplate formTemplate = new DateHistoryTemplate();
        formTemplate.setCcy(ccy);
        model.addAttribute("dateInputTemplate", formTemplate);
        model.addAttribute("currencyRatesList", historyList.getFxRate());
        return "currency-history";
    }


    @PostMapping("/getCurrencyHistoryCustom/{ccy}")
    public String getCurrencyHistoryCustom(@PathVariable(value = "ccy") CcyISO4217 ccy,
                                           @Valid @ModelAttribute("dateInputTemplate") DateHistoryTemplate formTemplate,
                                           BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusYears(1);
            lt.lb.webservices.fxrates.FxRatesHandling historyList = service.getCurrencyHistoryBase(ccy.toString(), startDate.toString(), endDate.toString());
            model.addAttribute("currencyRatesList", historyList.getFxRate());
        } else {
            FxRatesHandling historyList = service.getCurrencyHistoryBase(formTemplate.getCcy().toString(),
                    formTemplate.getStartDate().toString(),
                    formTemplate.getEndDate().toString());
            model.addAttribute("currencyRatesList", historyList.getFxRate());
        }
        model.addAttribute("dateInputTemplate", formTemplate);
        return "currency-history";
    }


    @GetMapping("/convertCurrency")
    public String convertCurrencyGet(Model model) throws DatatypeConfigurationException {
        model.addAttribute("answerActive", Boolean.FALSE);
        model.addAttribute("ccyComparatorAns", new CcyComparator());
        model.addAttribute("currencyNameList", service.getCurrentCurrencyRates().getFxRate());
        model.addAttribute("ccyComparator", new CcyComparator());
        return "convert-currency";
    }

    @PostMapping("/convertCurrency")
    public String convertCurrencyPost(@Valid @ModelAttribute("ccyComparator") CcyComparator compareHandler,
                                      BindingResult bindingResult, Model model) throws DatatypeConfigurationException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("answerActive", Boolean.FALSE);
            model.addAttribute("ccyComparatorAns", new CcyComparator());

        } else {
            model.addAttribute("answerActive", Boolean.TRUE);
            model.addAttribute("ccyComparatorAns", service.compareCcyRate(compareHandler));

        }
        model.addAttribute("currencyNameList", service.getCurrentCurrencyRates().getFxRate());
        model.addAttribute("ccyComparator", new CcyComparator());
        return "convert-currency";
    }


    private LocalDate convertToLocalDate(Date date) {
        LocalDate fixedDate = Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return fixedDate;
    }
}
