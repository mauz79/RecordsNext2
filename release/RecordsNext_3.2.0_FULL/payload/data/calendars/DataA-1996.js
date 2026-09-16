var dataGiornata = new Array()
// Immettere le date nel formato inglese: Mese Giorno Anno
// Gennaio = January
// Febbraio = February
// Marzo = March
// Maprile = April
// Maggio = May
// Giugno = June
// Luglio = July
// Agosto = August
// Settembre = September
// Ottobre = October
// Novembre = November
// Dicembre = December
dataGiornata[1] = "September 08 1996" 
dataGiornata[2] = "September 15 1996" 
dataGiornata[3] = "September 22 1996" 
dataGiornata[4] = "September 29 1996" 
dataGiornata[5] = "October 13 1996" 
dataGiornata[6] = "October 20 1996" 
dataGiornata[7] = "October 27 1996" 
dataGiornata[8] = "November 03 1996" 
dataGiornata[9] = "November 17 1996" 
dataGiornata[10] = "November 24 1996" 
dataGiornata[11] = "December 01 1996" 
dataGiornata[12] = "December 08 1996" 
dataGiornata[13] = "December 15 1996" 
dataGiornata[14] = "December 22 1996" 
dataGiornata[15] = "January 05 1997" 
dataGiornata[16] = "January 12 1997" 
dataGiornata[17] = "January 19 1997" 
dataGiornata[18] = "January 26 1997" 
dataGiornata[19] = "February 02 1997" 
dataGiornata[20] = "February 16 1997" 
dataGiornata[21] = "February 23 1997" 
dataGiornata[22] = "March 02 1997" 
dataGiornata[23] = "March 09 1997" 
dataGiornata[24] = "March 16 1997" 
dataGiornata[25] = "March 23 1997" 
dataGiornata[26] = "April 06 1997" 
dataGiornata[27] = "April 13 1997" 
dataGiornata[28] = "April 20 1997" 
dataGiornata[29] = "May 04 1997" 
dataGiornata[30] = "May 11 1997" 
dataGiornata[31] = "May 15 1997" 
dataGiornata[32] = "May 18 1997" 
dataGiornata[33] = "May 25 1997" 
dataGiornata[34] = "June 01 1997" 
function initArray() {  
	this.length = initArray.arguments.length
    for (var i = 0; i < this.length; i++)
    this[i+1] = initArray.arguments[i]
}
var DOWArray = new initArray("Dom","Lun","Mar","Mer","Gio","Ven","Sab")
var MOYArray = new initArray("Gen","Feb","Mar","Apr","Mag","Giu","Lug","Ago","Set","Ott","Nov","Dic")
var Year
for (t = 1; t < dataGiornata.length ;t++ ) {
	data = new Date(dataGiornata[t])
	Year = data.getYear()
	if (Year < 1996)
		Year = Year + 1900
	dataGiornata[t] = DOWArray[(data.getDay()+1)] + " " + data.getDate() + " " + MOYArray[(data.getMonth()+1)] + " " + Year
}
