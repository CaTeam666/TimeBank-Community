/**
 * 从身份证号中计算年龄
 * @param idCard 18位身份证号
 * @returns 返回年龄，如果格式不正确返回 -1
 */
export const calculateAge = (idCard: string): number => {
  if (!idCard || idCard.length !== 18) {
    return -1;
  }

  const birthYear = parseInt(idCard.substring(6, 10));
  const birthMonth = parseInt(idCard.substring(10, 12));
  const birthDay = parseInt(idCard.substring(12, 14));

  if (isNaN(birthYear) || isNaN(birthMonth) || isNaN(birthDay)) {
    return -1;
  }

  const today = new Date();
  let age = today.getFullYear() - birthYear;
  const monthDiff = today.getMonth() + 1 - birthMonth;

  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDay)) {
    age--;
  }

  return age;
};
