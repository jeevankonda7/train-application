package org.olympus.trainapp.controller;

import java.util.ArrayList;
import java.util.List;

import org.olympus.trainapp.dao.CreateJourneyDao;
import org.olympus.trainapp.dao.CreateTrainDao;
import org.olympus.trainapp.dto.CreateJourney;
import org.olympus.trainapp.dto.CreateTrain;
import org.olympus.trainapp.dto.DailyReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TrainJourneyController {

	@Autowired
	private CreateTrainDao trainDao;

	@Autowired
	private CreateJourneyDao journeyDao;

	@RequestMapping("/")
	public ModelAndView openPage(ModelAndView view) {
		List<CreateJourney> journeyList = journeyDao.findRecords();
		view.setViewName("search");
		view.addObject("journeyLists", journeyList);
		return view;
	}

	@RequestMapping(value = "/load")
	public String loadPages(String page, Model model) {
		model.addAttribute("t", new CreateTrain());
		return page;
	}

	@RequestMapping(value = "/savetrain")
	public String save(@ModelAttribute CreateTrain t) {
		trainDao.save(t);
		return "redirect:/";

	}

	@RequestMapping(value = "/open")
	public String loadJourney(String page, Model model) {
		List<CreateTrain> trains = trainDao.getAllTrainNames();
		model.addAttribute("trainlist", trains);
		model.addAttribute("journey", new CreateJourney());
		return page;
	}

	@RequestMapping(value = "/saverecords", method = RequestMethod.POST)
	public String saveOrUpdateRecord(@ModelAttribute CreateJourney journey) {
		System.out.println(journey.getId());
		if (journey.getId() != null) {
			journeyDao.updateJourneys(journey);
		} else {
			journeyDao.saveRecords(journey);
		}
		return "redirect:/";
	}

	@RequestMapping(value = "/openUpdate")
	public ModelAndView loadUpdateJourneyPage(@RequestParam("id") String id, ModelAndView view) {
		CreateJourney journey = journeyDao.findRecord(id);
		List<DailyReport> journeyreports = journeyDao.getAllReports(journey);
		List<CreateTrain> trains = trainDao.getAllTrainNames();
//		view.addObject("reportlist", journeyreports);
		view.addObject("journey", journey);
		view.addObject("trainlist", trains);
		if (journey != null && journey.getReport() != null) {
			List<DailyReport> reports = journey.getReport();
			view.addObject("reports", reports);
		}
		view.setViewName("newcreatejourney");
		return view;
	}

	@RequestMapping(value = "/delete")
	public String deleteJourney(@RequestParam String id) {
		journeyDao.delete(id);
		return "redirect:/";
	}

	@RequestMapping(value = "/searching")
	public String search(@RequestParam(required = false) String bookingid,
			@RequestParam(required = false) String agency, @RequestParam(required = false) String phone, Model model) {
		List<CreateJourney> bookings = journeyDao.searchJourneys(bookingid, agency, phone);
		model.addAttribute("journeyLists", bookings);
		return "search";
	}

	@RequestMapping(value = "/reset")
	public String resetSearch() {
		return "redirect:/";
	}

	@RequestMapping(value = "/cancel")
	public String cancel() {
		return "redirect:/";
	}

	@PostMapping("/validatePhoneNumberLength")
	@ResponseBody
	public String validatePhoneNumberLength(@RequestParam("phoneNumber") String phoneNumber) {
		return phoneNumber.length() != 10 ? "Phone number must be 10 digits" : "Phone number is valid";

	}

	@PostMapping("/checkTrainNumberAvailability")
	@ResponseBody
	public String checkTrainNumberAvailability(@RequestParam("trainNumber") String trainNumber) {
		boolean available = trainDao.isTrainNumberExists(Integer.valueOf(trainNumber));
		if (available) {
			return "Train number is already allocated";
		} else {
			return "";
		}
	}

	@GetMapping("/verifybookingid")
	@ResponseBody
	public String verifyBookingId(@RequestParam("bookingId") String id) {
		List<CreateJourney> list = journeyDao.findByBookingId(Integer.valueOf(id));
		return list.size() > 0 ? "BookingID already exists" : "";

	}

	@GetMapping("/verifydate")
	@ResponseBody
	public String verifyDate(@RequestParam("choosedate") String date) {
		return journeyDao.validateDate(date);
	}

}
