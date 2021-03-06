package core;

import java.util.Comparator;

public class JobDateComparator implements Comparator<Job>
{
    public int compare(Job p, Job q) {
        if ((p.getLastChangeDate() == null) && (q.getLastChangeDate() == null))
        {
            return 0;
        }
        else if ((p.getLastChangeDate() != null) && (q.getLastChangeDate() == null))
        {
            return -1;
        }
        else if ((p.getLastChangeDate() == null) && (q.getLastChangeDate() != null))
        {
            return 1;
        }
        else
        {
            if (p.getLastChangeDate().before(q.getLastChangeDate())) {
                return 1;
            } else if (p.getLastChangeDate().after(q.getLastChangeDate())) {
                return -1;
            } else {
                return 0;
            }
        }


//        if ((p.getJob().getLastChangeDate() != null) && (q.getJob().getLastChangeDate() != null)) {
//            if (p.getJob().getLastChangeDate().before(q.getJob().getLastChangeDate())) {
//                System.out.println("(before) return: -1");
//                return -1;
//            } else if (p.getJob().getLastChangeDate().after(q.getJob().getLastChangeDate())) {
//                System.out.println("(after) return: 1");
//                return 1;
//            } else {
//                System.out.println("return: 0");
//                return 0;
//            }
//        } else {
//            System.out.println("return: 0");
//            return 0;
//        }
    }
}
