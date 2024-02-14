package org.olympus.trainapp.dao;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.olympus.trainapp.dto.CreateJourney;
import org.olympus.trainapp.dto.CreateTrain;
import org.olympus.trainapp.dto.DailyReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CreateJourneyDao {

	@Autowired
	private Session session;

	@Autowired
	private CreateTrainDao trainDao;
	
	@Autowired
	private ReportDao reportDao;

	@Transactional
	public void saveRecords(CreateJourney journey) {
		session.save(journey);
		for (DailyReport report : journey.getReport()) {
			Transaction t = session.beginTransaction();
			report.setJourney(journey);
			Integer trainId = report.getTrain().getId();
			CreateTrain train = session.find(CreateTrain.class, trainId);
			report.setTrain(train);
			session.save(report);
			t.commit();
		}
	}

	public CreateJourney findRecord(String id) {
		Integer i = Integer.parseInt(id);
		CreateJourney journey = session.get(CreateJourney.class, i);
		if (journey != null)
			return journey;
		else
			return null;
	}

	public List<CreateJourney> findRecords() {
		String query = "select c from CreateJourney c";
		Query<CreateJourney> qs = session.createQuery(query);
		List<CreateJourney> journeyList = qs.getResultList();
		if (journeyList.size() > 0)
			return journeyList;
		else
			return null;
	}
	
	public void updateJourneys(CreateJourney journey) {
//	    List<DailyReport> dbData = reportDao.getAllReports(journey.getId()); 
	    List<DailyReport> dbData = getAllReports(journey); 
	    List<DailyReport> formData = journey.getReport(); 
	    Transaction t = session.beginTransaction();
	    session.clear();
	    session.saveOrUpdate(journey);

	    for (DailyReport dbReport : dbData) {
	        if (formData.contains(dbReport)) {
	            DailyReport matchingFormReport = formData.get(formData.indexOf(dbReport));
	            dbReport.setJourney(journey);
	            dbReport.setFrom_loc(matchingFormReport.getFrom_loc());
	            dbReport.setTo_loc(matchingFormReport.getTo_loc());
	            dbReport.setDate(matchingFormReport.getDate());
	            CreateTrain train = trainDao.findById(matchingFormReport.getTrain().getId());
	            dbReport.setTrain(train);
	            session.merge(dbReport);
	        } else {
	            session.delete(dbReport);
	        }
	    }

	    for (DailyReport formReport : formData) {
	        if (!dbData.contains(formReport)) {
	            CreateTrain train = trainDao.findById(formReport.getTrain().getId());
	            formReport.setTrain(train);
	            formReport.setJourney(journey);
	            session.saveOrUpdate(formReport); 
	        }
	    }

	    t.commit();
	}

	public void delete(String id) {
		CreateJourney journey = session.get(CreateJourney.class, Integer.parseInt(id));
		if(journey!=null) {
		Transaction t = session.beginTransaction();
			session.delete(journey);
			t.commit();
		}
		}

	public List<CreateJourney> searchJourneys(String bookingid, String agency, String phone) {
		try {
			if (bookingid != null && !bookingid.isEmpty()) {
				if ((agency != null && !agency.isEmpty()) && (phone != null && !phone.isEmpty())) {
					return searchByBookingIdAgencyAndPhone(Integer.valueOf(bookingid), agency, Long.valueOf(phone));
				} else if (agency != null && !agency.isEmpty()) {
					return searchByBookingIdAndAgency(Integer.valueOf(bookingid), agency);
				} else if (phone != null && !phone.isEmpty()) {
					return searchByBookingIdAndPhone(Integer.valueOf(bookingid), Long.valueOf(phone));
				} else {
					return findByBookingId(Integer.valueOf(bookingid));
				}
			} else if ((agency != null && !agency.isEmpty()) && (phone != null && !phone.isEmpty())) {
				return searchByAgencyAndPhone(agency, Long.valueOf(phone));
			} else if (agency != null && !agency.isEmpty()) {
				return findByAgency(agency);
			} else if (phone != null && !phone.isEmpty()) {
				return findByPhone(Long.valueOf(phone));
			} else {
				return null;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<CreateJourney> searchByBookingIdAgencyAndPhone(Integer bookingid, String agency, Long phone) {
		String query = "select j from CreateJourney j where j.bookingid=?1 and j.agency LIKE ?2 and j.phone=?3";
		String agencyPattern = "%" + agency + "%";
		Query<CreateJourney> q = session.createQuery(query);
		q.setParameter(1, bookingid);
		q.setParameter(2, agencyPattern);
		q.setParameter(3, phone);
		List<CreateJourney> result = q.getResultList();
		return result;

	}

	public List<CreateJourney> searchByAgencyAndPhone(String agency, Long phone) {
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<CreateJourney> criteriaQuery = criteriaBuilder.createQuery(CreateJourney.class);
		Root<CreateJourney> root = criteriaQuery.from(CreateJourney.class);

		criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("agency"), agency),
				criteriaBuilder.equal(root.get("phone"), phone));

		return session.createQuery(criteriaQuery).getResultList();
	}

	public List<CreateJourney> findByPhone(Long phone) {
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<CreateJourney> criteriaQuery = criteriaBuilder.createQuery(CreateJourney.class);
		Root<CreateJourney> root = criteriaQuery.from(CreateJourney.class);
		criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("phone"), phone));
		return session.createQuery(criteriaQuery).getResultList();
	}

	public List<CreateJourney> findByBookingId(Integer id) {
		Query<CreateJourney> query = session.createQuery("select c from CreateJourney c where c.bookingid=?1");
		query.setParameter(1, id);
		return query.getResultList();
	}

	public List<CreateJourney> searchByBookingIdAndAgency(Integer bookingid, String agency) {
		Query<CreateJourney> query = session
				.createQuery("select j from CreateJourney j where j.bookingid=?1 and j.agency=?2");
		query.setParameter(1, bookingid);
		query.setParameter(2, agency);
		return query.getResultList();
	}

	public List<CreateJourney> searchByBookingIdAndPhone(Integer bookingid, Long phone) {
		Query<CreateJourney> query = session
				.createQuery("select j from CreateJourney j where j.bookingid=?1 and j.phone=?2");
		query.setParameter(1, bookingid);
		query.setParameter(2, phone);
		return query.getResultList();
	}

	public List<CreateJourney> findByAgency(String agency) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<CreateJourney> criteriaQuery = builder.createQuery(CreateJourney.class);
		Root<CreateJourney> root = criteriaQuery.from(CreateJourney.class);
		Predicate agencyPredicate = builder.like(root.get("agency").as(String.class), "%" + agency + "%");
		criteriaQuery.select(root).where(agencyPredicate);

		return session.createQuery(criteriaQuery).getResultList();
	}
	
	public List<DailyReport> getAllReports(CreateJourney journey){
		String query = "select r from DailyReport r where r.journey.id = ?1";
		Query<DailyReport> q = session.createQuery(query);
		q.setParameter(1, journey.getId());
		return q.getResultList();
	}
	
	public String validateDate(String date) {
		LocalDate d1 = LocalDate.now();
		LocalDate d2 = LocalDate.parse(date);
		if(d2.isBefore(d1))
			return "invalid";
		else
			return "";
	}

}
