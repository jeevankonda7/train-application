package org.olympus.trainapp.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.olympus.trainapp.dto.DailyReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ReportDao {
	
	@Autowired
	private Session session;
	
	@Autowired
	private CreateTrainDao trainDao;
	
	@Autowired
	private CreateJourneyDao journeyDao;
	
	public List<DailyReport> getAllReports(Integer id) {
		String query = "select d from DailyReport d where d.journey.id=?1";
		Query<DailyReport> reports = session.createQuery(query);
		reports.setParameter(1, id);
		return reports.getResultList();
	}

}
